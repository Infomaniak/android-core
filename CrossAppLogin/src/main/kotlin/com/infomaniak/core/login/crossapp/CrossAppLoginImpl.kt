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

import android.content.Context
import android.content.Intent
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import com.infomaniak.core.android.service.OnBindingIssue
import com.infomaniak.core.android.service.OnServiceDisconnectionBehavior
import com.infomaniak.core.android.service.withBoundService
import com.infomaniak.core.cancellable
import com.infomaniak.core.login.crossapp.internal.ChannelMessageHandler
import com.infomaniak.core.login.crossapp.internal.DisposableMessage
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateChecker
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateCheckerImpl
import com.infomaniak.core.login.crossapp.internal.certificates.AppSigningCertificates
import com.infomaniak.core.login.crossapp.internal.certificates.infomaniakAppsCertificates
import com.infomaniak.lib.core.utils.SentryLog
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import splitties.init.appCtx

internal class CrossAppLoginImpl : CrossAppLogin {

    private val appSigningCertificates: AppSigningCertificates = infomaniakAppsCertificates

    private val certificateChecker: AppCertificateChecker = AppCertificateCheckerImpl(
        signingCertificates = appSigningCertificates
    )
    private val ourPackageName = appCtx.packageName

    @ExperimentalSerializationApi
    override suspend fun retrieveAccountsFromOtherApps(): List<ExternalAccount> {
        val lists: List<List<ExternalAccount>> = coroutineScope {
            appSigningCertificates.packageNames.mapNotNull { packageName ->
                if (packageName == ourPackageName) null
                else async { retrieveAccountsFromApp(packageName) }
            }.awaitAll()
        }
        return lists.flatten().groupBy { it.email }.map { (_, externalAccounts) ->
            val account = externalAccounts.firstOrNull { it.isCurrentlySelectedInAnApp } ?: externalAccounts.first()
            account.copy(tokens = externalAccounts.flatMapTo(mutableSetOf()) { it.tokens })
        }
    }

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromApp(packageName: String): List<ExternalAccount> = raceOf(
        {
            val isTrustedApp = certificateChecker.isPackageNameAllowed(packageName) == true
            if (isTrustedApp) awaitCancellation()
            emptyList() // Return early if the app isn't trusted, or not installed.
        },
        {
            // Start connecting to the app immediately, during the signing certificate check.
            val externalAccounts = retrieveAccountsFromUncheckedApp(packageName)
            // Check we can trust the result, or drop it.
            val isTrustedApp = certificateChecker.isPackageNameAllowed(packageName) == true
            if (isTrustedApp) externalAccounts else emptyList()
        }
    )

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromUncheckedApp(packageName: String): List<ExternalAccount> {
        val intent = Intent().also {
            it.`package` = packageName
            it.action = "com.infomaniak.crossapp.login"
        }
        //TODO: Add a timeout for the entire operation.
        return runCatching {
            retrieveAccountsFromUncheckedService(intent)
        }.cancellable().getOrElse { t ->
            if (t is RemoteException) { //TODO: Can this really happen here? They are probably caught inside.
                SentryLog.i(TAG, "The app ($packageName) we tried to retrieve accounts from crashed", t)
            } else {
                SentryLog.e(TAG, "Couldn't retrieve accounts in app $packageName", t)
            }
            emptyList()
        }
    }

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromUncheckedService(service: Intent): List<ExternalAccount> {
        val bytesOrNull = appCtx.withBoundService<ByteArray?>(
            service = service,
            timeoutConnection = { isWaitingForReconnect -> OnBindingIssue.GiveUp(null) },
            onDisconnected = { OnServiceDisconnectionBehavior.UnbindImmediately(null) },
            onBindingIssue = { OnBindingIssue.GiveUp(null) },
            flags = Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT,
            block = { binder ->
                val messenger = Messenger(binder)
                val replies = Channel<DisposableMessage>(capacity = 1)
                val replyHandler = ChannelMessageHandler(replies)
                val replyTo = Messenger(replyHandler)

                try {
                    val request = Message.obtain().also {
                        it.what = BaseCrossAppLoginService.IpcMessageWhat.GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS
                        it.replyTo = replyTo
                    }
                    messenger.send(request)
                    replies.receive().use { response ->
                        response.obj as ByteArray
                    }
                } catch (_: RemoteException) {
                    null
                }
            },
        )

        return bytesOrNull?.let { bytes -> ProtoBuf.decodeFromByteArray<List<ExternalAccount>>(bytes) } ?: emptyList()
    }

    private companion object {
        const val TAG = "CrossAppLoginImpl"
    }
}
