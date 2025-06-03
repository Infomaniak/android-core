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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import splitties.coroutines.launchRacer
import splitties.coroutines.race
import splitties.coroutines.raceOf
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
        if (uid == -1) return false
        return allowedUids.useElement(key = uid) { it.await() }
    }

    private suspend fun checkIfUidShouldBeAllowed(uid: Int): Boolean {
        val packages = Dispatchers.IO { packageManager.getPackagesForUid(uid) } ?: return false

        val finishedCheckers = MutableStateFlow(0)
        return race {
            packages.forEach { packageToCheck ->
                launchRacer {
                    val allowed = checkIfPackageMatchesAnyCertificate(packageToCheck)
                    if (!allowed) {
                        finishedCheckers.update { it + 1 }
                        awaitCancellation()
                    }
                    true
                }
            }
            launchRacer {
                finishedCheckers.first { it == packages.size }
                false
            }
        }
    }

    private suspend fun checkIfPackageMatchesAnyCertificate(targetPackageName: String): Boolean {
        val signatures: List<Signature> = if (SDK_INT >= 28) {
            val pkgInfo = Dispatchers.IO {
                packageManager.getPackageInfo(targetPackageName, PackageManager.GET_SIGNING_CERTIFICATES)
            }
            val signingInfo = pkgInfo?.signingInfo
            when {
                signingInfo == null -> emptyList()
                signingInfo.hasMultipleSigners() -> signingInfo.apkContentsSigners.asList()
                else -> signingInfo.signingCertificateHistory.asList()
            }
        } else @Suppress("Deprecation") {
            val pkgInfo = Dispatchers.IO {
                packageManager.getPackageInfo(targetPackageName, PackageManager.GET_SIGNATURES)
            }
            pkgInfo?.signatures?.asList() ?: emptyList()
        }
        if (signatures.isEmpty()) return false

        val matchedCompletable: CompletableJob = Job()
        return raceOf({
            matchedCompletable.join()
            true
        },  {
            signatures.forEach {
                async {
                    val matches = signingCertificates.matches(targetPackageName = targetPackageName, signingCertificate = it)
                    if (matches) {
                        matchedCompletable.complete()
                        awaitCancellation()
                    }
                }
            }
            false // Will not be accepted if there's a match since we'd awaitCancellation for one of the child coroutines.
        })
    }
}
