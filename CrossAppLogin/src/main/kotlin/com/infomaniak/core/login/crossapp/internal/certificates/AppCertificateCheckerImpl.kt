/*
 * Infomaniak Login - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core.login.crossapp.internal.certificates

import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build.VERSION.SDK_INT
import com.infomaniak.core.DynamicLazyMap
import com.infomaniak.core.completableScope
import kotlinx.coroutines.*
import splitties.experimental.ExperimentalSplittiesApi
import splitties.init.appCtx

internal class AppCertificateCheckerImpl(
    private val signingCertificates: AppSigningCertificates
) : AppCertificateChecker {

    private val packageManager = appCtx.packageManager
    private val allowedUids = DynamicLazyMap<Int, Deferred<Boolean>>(
        cacheManager = { _, _ -> awaitCancellation() },
        createElement = { uid -> async { checkIfUidShouldBeAllowed(uid) } }
    )

    override suspend fun isUidAllowed(uid: Int): Boolean {
        if (uid == -1) return false // -1 means the message is invalid, see android.os.Message.sendingUid Javadoc.
        return allowedUids.useElement(key = uid) { it.await() }
    }

    private suspend fun checkIfUidShouldBeAllowed(uid: Int): Boolean {
        val packages = Dispatchers.IO { packageManager.getPackagesForUid(uid) } ?: return false

        return completableScope { completable ->
            packages.forEach { packageToCheck ->
                launch {
                    val allowed = checkIfPackageMatchesAnyCertificate(packageToCheck)
                    if (allowed) completable.complete(true)
                    true
                }
            }
            false // Will make it only if there is no match after all child async coroutines complete.
        }
    }

    private suspend fun checkIfPackageMatchesAnyCertificate(targetPackageName: String): Boolean {
        val signatures: List<Signature> = if (SDK_INT >= 28) {
            val signingInfo = Dispatchers.IO {
                packageManager.getPackageInfo(targetPackageName, PackageManager.GET_SIGNING_CERTIFICATES)
            }?.signingInfo

            when {
                signingInfo == null -> emptyList()
                signingInfo.hasMultipleSigners() -> signingInfo.apkContentsSigners.asList()
                else -> signingInfo.signingCertificateHistory?.asList() ?: emptyList()
            }
        } else @Suppress("Deprecation") {
            Dispatchers.IO {
                packageManager.getPackageInfo(targetPackageName, PackageManager.GET_SIGNATURES)
            }?.signatures?.asList() ?: emptyList()
        }
        if (signatures.isEmpty()) return false

        return completableScope { completable ->
            signatures.forEach {
                launch {
                    val matches = signingCertificates.matches(targetPackageName = targetPackageName, signingCertificate = it)
                    if (matches) completable.complete(true)
                }
            }
            false // Will make it only if there is no match after all child coroutines complete.
        }
    }
}
