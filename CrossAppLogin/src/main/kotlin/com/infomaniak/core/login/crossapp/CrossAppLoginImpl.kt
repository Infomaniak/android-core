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

package com.infomaniak.core.login.crossapp

import com.infomaniak.core.DynamicLazyMap
import com.infomaniak.core.login.crossapp.internal.certificates.AppSigningCertificates
import com.infomaniak.core.login.crossapp.internal.certificates.infomaniakAppsCertificates
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import splitties.init.appCtx

internal class CrossAppLoginImpl : CrossAppLogin {

    private val appSigningCertificates: AppSigningCertificates = infomaniakAppsCertificates
    private val ourPackageName = appCtx.packageName
    private val packageManager = appCtx.packageManager

    private val validatedApps = DynamicLazyMap<String, Deferred<Boolean?>>(
        cacheManager = { packageName, isValidatedDeferred ->
            val isValidated = isValidatedDeferred.await()
            if (isValidated != null) {
                awaitCancellation()
            } // null means the app isn't installed. We don't want to cache the value in that case.
        },
    ) { key ->
        async { TODO("Check if installed and if matches") }
    }

    @ExperimentalSerializationApi
    override suspend fun retrieveAccountsFromOtherApps(): List<ExternalAccount> {
        val lists: List<List<ExternalAccount>> = coroutineScope {
            appSigningCertificates.packageNames.mapNotNull { packageName ->
                if (packageName == ourPackageName) null
                else async { retrieveAccountsFromApp(packageName) }
            }.awaitAll()
        }
        return lists.asSequence().flatten().groupBy { it.email }.map { (_, externalAccounts) ->
            externalAccounts.firstOrNull { it.isCurrentlySelectedInAnApp } ?: externalAccounts.first()
            //TODO: Fuse tokens
        }
    }

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromApp(packageName: String): List<ExternalAccount> = validatedApps.useElement(
        key = packageName
    ) { isTrustedDeferred ->
        raceOf({
            val isTrustedApp = isTrustedDeferred.await() == true
            if (isTrustedApp) awaitCancellation()
            emptyList() // Return early if the app isn't trusted, or not installed.
        }, {
            TODO("Connect to the given app ASAP")
            // Check we can trust the result, or drop it.
            val isTrustedApp = isTrustedDeferred.await() == true
            if (isTrustedApp) TODO("The result we got") else emptyList()
        })
    }
}
