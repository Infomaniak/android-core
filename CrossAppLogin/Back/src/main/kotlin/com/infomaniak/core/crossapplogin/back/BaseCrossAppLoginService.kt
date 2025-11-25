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
@file:OptIn(ExperimentalSerializationApi::class)

package com.infomaniak.core.crossapplogin.back

import android.content.Intent
import android.os.DeadObjectException
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Process
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.crossapplogin.back.internal.ChannelMessageHandler
import com.infomaniak.core.crossapplogin.back.internal.DisposableMessage
import com.infomaniak.core.crossapplogin.back.internal.certificates.AppCertificateChecker
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdManager
import com.infomaniak.core.crossapplogin.back.internal.localAccountsFlow
import com.infomaniak.core.sentry.SentryLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class BaseCrossAppLoginService(private val selectedUserIdFlow: Flow<Int?>) : LifecycleService() {

    private val incomingMessagesChannel = Channel<DisposableMessage>(capacity = Channel.UNLIMITED)
    private val messagesHandler = ChannelMessageHandler(incomingMessagesChannel)
    private val messenger by lazy { Messenger(messagesHandler) }

    private val certificateChecker = AppCertificateChecker.withInfomaniakApps

    private val signedInAccountRequests = Channel<Messenger>(capacity = Channel.UNLIMITED)
    private val syncSharedDeviceIdRequests = Channel<ByteArray>(capacity = Channel.UNLIMITED)

    init {
        lifecycleScope.launch { handleIncomingMessages() }
        lifecycleScope.launch { handleSignedInAccountsRequests() }
    }

    final override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return messenger.binder
    }

    private suspend fun handleIncomingMessages() = Dispatchers.Default {
        val ourUid = Process.myUid()

        incomingMessagesChannel.consumeAsFlow().onEach { disposableMessage ->
            disposableMessage.use { msg ->
                check(msg.sendingUid != ourUid) // We are not supposed to talk to ourselves.
                if (certificateChecker.isUidAllowed(msg.sendingUid).not()) return@use

                val trustedClientMessenger = msg.replyTo
                when (msg.what) {
                    IpcMessageWhat.GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS -> {
                        check(signedInAccountRequests.trySend(trustedClientMessenger).isSuccess)
                    }
                    IpcMessageWhat.GET_SHARED_DEVICE_ID -> {
                        val requestData = runCatching {
                            ProtoBuf.decodeFromByteArray<CrossAppDeviceIdRequest>(msg.unwrapByteArrayOrNull()!!)
                        }.getOrElse { t ->
                            SentryLog.wtf(TAG, "GET_SHARED_DEVICE_ID message didn't contain expected data", t)
                            return@use
                        }
                        launch { returnSharedDeviceIdOrWaitForSync(trustedClientMessenger, requestData) }
                    }
                    IpcMessageWhat.SYNC_SHARED_DEVICE_ID -> {
                        val sharedId: ByteArray = runCatching { msg.unwrapByteArrayOrNull()!! }.getOrElse { t ->
                            SentryLog.wtf(TAG, "SYNC_SHARED_DEVICE_ID message didn't contain a proper string id", t)
                            return@use
                        }
                        syncSharedDeviceIdRequests.trySend(sharedId)
                    }
                }
            }
        }.onCompletion {
            messagesHandler.removeCallbacksAndMessages(null)
        }.collect()
    }

    private suspend fun handleSignedInAccountsRequests() = Dispatchers.Default {
        val accountsDataFlow: SharedFlow<ByteArray> = localAccountsFlow(selectedUserIdFlow)
            .map { ProtoBuf.Default.encodeToByteArray(it) }
            .shareIn(this, SharingStarted.Eagerly, replay = 1)
        for (trustedClient in signedInAccountRequests) {
            sendSignedInAccountsToApp(
                accountsDataFlow = accountsDataFlow,
                trustedClientMessenger = trustedClient
            )
        }
    }

    private suspend fun returnSharedDeviceIdOrWaitForSync(
        clientMessenger: Messenger,
        requestData: CrossAppDeviceIdRequest,
    ) {
        val sharedDeviceIdManager = CrossAppLogin.forContext(this, lifecycleScope).sharedDeviceIdManager

        sharedDeviceIdManager.findCrossAppDeviceId(requestData)?.let { sharedId ->
            sendSharedDeviceId(clientMessenger, sharedId)
            return
        }

        val sharedIdToUse = Uuid.fromByteArray(syncSharedDeviceIdRequests.receive())
        SharedDeviceIdManager.sharedDeviceIdMutex.withLock {
            SharedDeviceIdManager.storage.setDeviceId(sharedIdToUse)
        }
    }

    private fun sendSharedDeviceId(destination: Messenger, id: Uuid) {
        destination.trySending { newMessage -> newMessage.putBundleWrappedDataInObj(id.toByteArray()) }
    }

    private suspend fun sendSignedInAccountsToApp(
        accountsDataFlow: SharedFlow<ByteArray>,
        trustedClientMessenger: Messenger
    ) {
        val accountsData = accountsDataFlow.first()
        trustedClientMessenger.trySending { newMessage -> newMessage.putBundleWrappedDataInObj(accountsData) }
    }

    private inline fun Messenger.trySending(configureMessage: (Message) -> Unit): Boolean {
        val reply = Message.obtain().also { msg ->
            msg.isAsynchronous = true
            configureMessage(msg)
        }
        return try {
            send(reply)
            true
        } catch (_: DeadObjectException) {
            // The client app died before we could respond. Usually, we can safely ignore it.
            false
        }
    }

    internal object IpcMessageWhat {
        const val GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS = 0
        const val GET_SHARED_DEVICE_ID = 1
        const val SYNC_SHARED_DEVICE_ID = 2
    }

    companion object {
        const val ACTION = "com.infomaniak.crossapp.login"
        private const val TAG = "BaseCrossAppLoginService"
    }
}
