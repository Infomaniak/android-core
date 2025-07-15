/*
 * Infomaniak Login - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
@file:OptIn(ExperimentalSerializationApi::class)

package com.infomaniak.core.login.crossapp

import android.content.Intent
import android.os.DeadObjectException
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Process
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.login.crossapp.internal.ChannelMessageHandler
import com.infomaniak.core.login.crossapp.internal.DisposableMessage
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateChecker
import com.infomaniak.core.login.crossapp.internal.deviceid.SharedDeviceIdManager
import com.infomaniak.core.login.crossapp.internal.localAccountsFlow
import com.infomaniak.lib.core.utils.SentryLog
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
abstract class BaseCrossAppLoginService(private val selectedUserIdFlow: Flow<Int>) : LifecycleService() {

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
        val sharedDeviceIdManager = CrossAppLogin.forContext(this).sharedDeviceIdManager

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
