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
import android.os.*
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.login.crossapp.internal.ChannelMessageHandler
import com.infomaniak.core.login.crossapp.internal.DisposableMessage
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateChecker
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateCheckerImpl
import com.infomaniak.core.login.crossapp.internal.certificates.infomaniakAppsCertificates
import com.infomaniak.core.login.crossapp.internal.localAccountsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

abstract class BaseCrossAppLoginService : LifecycleService() {

    protected abstract val selectedUserIdFlow: Flow<Int>

    private val incomingMessagesChannel = Channel<DisposableMessage>(capacity = Channel.UNLIMITED)
    private val messagesHandler = ChannelMessageHandler(incomingMessagesChannel)
    private val messenger by lazy { Messenger(messagesHandler) }

    private val certificateChecker: AppCertificateChecker = AppCertificateCheckerImpl(
        signingCertificates = infomaniakAppsCertificates
    )

    internal object MsgWhat {
        const val GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS = 0
    }

    init {
        lifecycleScope.launch { handleIncomingMessages() }
    }

    final override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return messenger.binder
    }

    private suspend fun handleIncomingMessages() = Dispatchers.Default {
        val ourUid = Process.myUid()
        val protobuf = ProtoBuf
        val accountsDataFlow: SharedFlow<ByteArray> = localAccountsFlow(selectedUserIdFlow).map {
            protobuf.encodeToByteArray(it)
        }.shareIn(this, SharingStarted.Eagerly, replay = 1)
        incomingMessagesChannel.consumeAsFlow().onEach { disposableMessage ->
            disposableMessage.use { msg ->
                check(msg.sendingUid != ourUid) // We are not supposed to talk to ourselves.
                if (certificateChecker.isUidAllowed(msg.sendingUid)) when (msg.what) {
                    MsgWhat.GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS -> {
                        val accountsData = accountsDataFlow.first()
                        val reply = Message.obtain().also { newMsg ->
                            newMsg.obj = accountsData
                            newMsg.isAsynchronous = true
                        }
                        try {
                            msg.replyTo.send(reply)
                        } catch (_: DeadObjectException) {
                            // The client app died before we could respond, we can safely ignore it.
                        }
                    }
                }
            }
        }.onCompletion {
            messagesHandler.removeCallbacksAndMessages(null)
        }.collect()
    }
}
