/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core.crossapplogin.back.internal.certificates

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build.VERSION.SDK_INT
import com.infomaniak.core.DynamicLazyMap
import com.infomaniak.core.completableScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import splitties.experimental.ExperimentalSplittiesApi
import splitties.init.appCtx

internal class AppCertificateCheckerImpl internal constructor(
    signingCertificates: AppSigningCertificates,
    private val coroutineScope: CoroutineScope,
) : AppCertificateChecker(signingCertificates) {

    private val packageManager = appCtx.packageManager

    private val validatedApps = DynamicLazyMap<String, Deferred<Boolean?>>(
        cacheManager = { packageName, isValidatedDeferred ->
            val isValidated = isValidatedDeferred.await()
            // null means the app isn't installed. We don't want to cache the value in that case.
            if (isValidated != null) awaitCancellation()
        },
        coroutineScope = coroutineScope,
    ) { packageName ->
        async { checkIfPackageMatchesAnyCertificate(packageName) }
    }

    private val allowedUids = DynamicLazyMap<Int, Deferred<Boolean?>>(
        cacheManager = { uid, isAllowedDeferred ->
            val isAllowed = isAllowedDeferred.await()
            // null means the app went away while checking the uid. We don't want to cache the value in that case.
            if (isAllowed != null) awaitCancellation()
        },
        coroutineScope = coroutineScope,
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
            val signingInfo = getPackageInfoOrNull(targetPackageName, PackageManager.GET_SIGNING_CERTIFICATES)?.signingInfo
            when {
                signingInfo == null -> emptyList()
                signingInfo.hasMultipleSigners() -> signingInfo.apkContentsSigners.asList()
                else -> signingInfo.signingCertificateHistory?.asList() ?: emptyList()
            }
        } else @Suppress("Deprecation") {
            getPackageInfoOrNull(targetPackageName, PackageManager.GET_SIGNATURES)?.signatures?.asList() ?: emptyList()
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

    private suspend fun getPackageInfoOrNull(targetPackageName: String, flags: Int): PackageInfo? = Dispatchers.IO {
        try {
            packageManager.getPackageInfo(targetPackageName, flags)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
