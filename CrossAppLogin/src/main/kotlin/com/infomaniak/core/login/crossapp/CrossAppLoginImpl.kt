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
import com.infomaniak.lib.core.utils.SentryLog
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration.Companion.seconds

internal class CrossAppLoginImpl(private val context: Context) : CrossAppLogin {

    private val certificateChecker = AppCertificateChecker.withInfomaniakApps
    private val ourPackageName = context.packageName
    private val targetPackageNames = certificateChecker.signingCertificates.packageNames.filter { it != ourPackageName }

    @ExperimentalSerializationApi
    override suspend fun retrieveAccountsFromOtherApps(): List<ExternalAccount> {
        val lists: List<List<ExternalAccount>> = coroutineScope {
            targetPackageNames.map { packageName ->
                async { retrieveAccountsFromApp(packageName) }
            }.awaitAll()
        }

        return lists.flatten().groupBy { it.email }.map { (_, externalAccounts) ->
            val account = externalAccounts.firstOrNull { it.isCurrentlySelectedInAnApp } ?: externalAccounts.first()
            account.copy(tokens = externalAccounts.flatMapTo(mutableSetOf()) { it.tokens })
        }
    }

    override suspend fun getAppGroupScopedDeviceInstanceId() {
        // TODO:
        //  1. If we already have an id, return it, otherwise, continue.
        //  2. Acquire `sharedDeviceIdLock`
        //  3. If we now have an id, return it, otherwise, continue.
        //  4. Generate id for proposal.
        //  5. Contact all known apps with the "START_SHARED_DEVICE_ID_SYNC" message, giving them the list of apps we're reaching //REMOVE THIS?, and the id proposal
        //    a. If we already have an id, return it, otherwise, continue.
        //    b. Acquire our `sharedDeviceIdLock`
        //    c. If we now have an id, return it (releasing the sharedDeviceLock), otherwise, continue.
        //    d. If there are other other apps that we know but didn't appear in the given list: ask them and return the result combination.
        //    e. Otherwise return that we don't have the id, and wait (with timeout for the finish event with the id to persist).
        //  6. Wait until one app gives us an id, or until all apps reply they don't have it
        //  7. Persist our sharedDeviceId to the returned one (if a contacted app gave us one), or to the one we initially generated.
        //  8. Release `sharedDeviceIdLock`
        //  9. Send "FINISH_SHARED_DEVICE_ID_SYNC" to all apps that didn't have the shared device id already, with the id and the entire list of checked apps.
        //    a. Persist the given shared device id.
        //    b. Release `sharedDeviceIdLock`
        //    c. Forward the message to all apps we know that weren't checked and didn't have the shared id yet.

        // TODO: Handle non updated apps (ignore them).
        // TODO: Handle apps being updated, by retrying if we detect the lastUpdateTime changed, or if we find a packageInstaller update session.
        TODO("Not yet implemented")
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
        // TODO: Add a timeout for the entire operation.
        return runCatching {
            retrieveAccountsFromUncheckedService(intent)
        }.cancellable().getOrElse { t ->
            if (t is RemoteException) { // TODO: Can this really happen here? They are probably caught inside.
                SentryLog.i(TAG, "The app ($packageName) we tried to retrieve accounts from crashed", t)
            } else {
                SentryLog.e(TAG, "Couldn't retrieve accounts in app $packageName", t)
            }
            emptyList()
        }
    }

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromUncheckedService(service: Intent): List<ExternalAccount> {
        val bytesOrNull = context.withBoundService<ByteArray?>(
            service = service,
            timeoutConnection = { isWaitingForReconnect ->
                delay(7.seconds)
            },
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
                    replies.receive().use { response -> response.unwrapByteArrayOrNull() }
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
