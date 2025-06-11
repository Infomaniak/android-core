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

    private val validatedApps = DynamicLazyMap<String, Deferred<Boolean?>>(
        cacheManager = { packageName, isValidatedDeferred ->
            val isValidated = isValidatedDeferred.await()
            if (isValidated != null) {
                awaitCancellation()
            } // null means the app isn't installed. We don't want to cache the value in that case.
        },
    ) { packageName ->
        async { checkIfPackageMatchesAnyCertificate(packageName) }
    }

    private val allowedUids = DynamicLazyMap<Int, Deferred<Boolean?>>(
        cacheManager = { uid, isAllowedDeferred ->
            val isAllowed = isAllowedDeferred.await()
            if (isAllowed != null) {
                awaitCancellation()
            } // null means the app went away while checking the uid. We don't want to cache the value in that case.
        },
        createElement = { uid -> async { checkIfUidShouldBeAllowed(uid) } }
    )

    override suspend fun isUidAllowed(uid: Int): Boolean {
        if (uid == -1) return false // -1 means the message is invalid, see android.os.Message.sendingUid Javadoc.
        return allowedUids.useElement(key = uid) { it.await() ?: false }
    }

    override suspend fun isPackageNameAllowed(packageName: String): Boolean? {
        validatedApps.useElement(packageName) { return it.await() }
    }

    private suspend fun checkIfUidShouldBeAllowed(uid: Int): Boolean? {
        val packages = Dispatchers.IO { packageManager.getPackagesForUid(uid) } ?: return false

        return completableScope { completable ->
            val unknown: Boolean = packages.map { packageToCheck ->
                async {
                    val allowed = isPackageNameAllowed(packageToCheck)
                    if (allowed == true) completable.complete(true) // Fast path
                    allowed
                }
            }.awaitAll().any { it == null }
            // Will make it there only if there is no match after all child async coroutines complete.
            if (unknown) null else false
        }
    }

    private suspend fun checkIfPackageMatchesAnyCertificate(targetPackageName: String): Boolean? {
        val signatures: List<Signature> = if (SDK_INT >= 28) {
            val signingInfo = Dispatchers.IO {
                try {
                    packageManager.getPackageInfo(targetPackageName, PackageManager.GET_SIGNING_CERTIFICATES)
                } catch (_: PackageManager.NameNotFoundException) {
                    null
                }
            }?.signingInfo

            when {
                signingInfo == null -> emptyList()
                signingInfo.hasMultipleSigners() -> signingInfo.apkContentsSigners.asList()
                else -> signingInfo.signingCertificateHistory?.asList() ?: emptyList()
            }
        } else @Suppress("Deprecation") {
            Dispatchers.IO {
                try {
                    packageManager.getPackageInfo(targetPackageName, PackageManager.GET_SIGNATURES)
                } catch (_: PackageManager.NameNotFoundException) {
                    null
                }
            }?.signatures?.asList() ?: emptyList()
        }
        if (signatures.isEmpty()) return null

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
