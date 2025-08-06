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

package com.infomaniak.core.crossapplogin.back

import android.content.Context
import android.content.Intent
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import com.infomaniak.core.android.service.OnBindingIssue
import com.infomaniak.core.android.service.OnServiceDisconnectionBehavior
import com.infomaniak.core.android.service.withBoundService
import com.infomaniak.core.cancellable
import com.infomaniak.core.crossapplogin.back.internal.ChannelMessageHandler
import com.infomaniak.core.crossapplogin.back.internal.DisposableMessage
import com.infomaniak.core.crossapplogin.back.internal.certificates.AppCertificateChecker
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdManager
import com.infomaniak.lib.core.room.UserDatabase
import com.infomaniak.lib.core.utils.SentryLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.invoke
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class CrossAppLoginImpl(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : CrossAppLogin() {

    private val certificateChecker = AppCertificateChecker.withInfomaniakApps
    private val ourPackageName = context.packageName
    private val targetPackageNames = certificateChecker.signingCertificates.packageNames.filter { it != ourPackageName }

    private val ipcIssuesManager: IpcIssuesManager = IpcIssuesManagerImpl

    @ExperimentalSerializationApi
    override suspend fun retrieveAccountsFromOtherApps(): List<ExternalAccount> {
        val lists: List<List<ExternalAccount>> = Dispatchers.Default {
            targetPackageNames.map { packageName ->
                async { retrieveAccountsFromApp(packageName) }
            }.awaitAll()
        }

        val alreadyConnectedEmails = UserDatabase().userDao().allUsers.first().map { it.email }

        return lists.flatten()
            .filter { it.email !in alreadyConnectedEmails }
            .groupBy { it.email }
            .map { (_, externalAccounts) ->
                val account = externalAccounts.firstOrNull { it.isCurrentlySelectedInAnApp } ?: externalAccounts.first()
                account.copy(tokens = externalAccounts.flatMapTo(mutableSetOf()) { it.tokens })
            }
            .sortedBy { it.isCurrentlySelectedInAnApp } // false comes before true, so selected accounts will be last in the list.
    }

    @ExperimentalUuidApi
    override val sharedDeviceIdFlow: SharedFlow<Uuid> get() = sharedDeviceIdManager.crossAppDeviceIdFlow

    @ExperimentalUuidApi
    override val sharedDeviceIdManager: SharedDeviceIdManager = SharedDeviceIdManager(
        context = context,
        coroutineScope = coroutineScope,
        ipcIssuesManager = ipcIssuesManager,
        certificateChecker = certificateChecker,
        targetPackageNames = targetPackageNames.toSet()
    )

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
        return runCatching {
            retrieveAccountsFromUncheckedService(intent)
        }.cancellable().getOrElse { t ->
            SentryLog.e(TAG, "Couldn't retrieve accounts in app $packageName", t)
            emptyList()
        }
    }

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromUncheckedService(service: Intent): List<ExternalAccount> {
        val targetPackageName = service.`package`!!
        val bytesOrNull = context.withBoundService<ByteArray?>(
            service = service,
            timeoutConnection = ipcIssuesManager.timeoutConnection(targetPackageName, isUserWaiting = true),
            onDisconnected = { OnServiceDisconnectionBehavior.UnbindImmediately(null) },
            onBindingIssue = {
                ipcIssuesManager.logBindingIssue(targetPackageName, it)
                OnBindingIssue.GiveUp(null)
            },
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
                    raceOf(
                        { replies.receive().use { response -> response.unwrapByteArrayOrNull() } },
                        {
                            ipcIssuesManager.timeoutOperation(
                                operationName = "retrieveAccountsFromUncheckedService",
                                targetPackageName = targetPackageName,
                                acceptableDuration = 10.seconds, // This long in case I/O gets very slow on low-end devices.
                            )
                            null
                        }
                    )
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
