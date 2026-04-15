/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Process
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.common.trySending
import com.infomaniak.core.crossapplogin.back.internal.ChannelMessageHandler
import com.infomaniak.core.crossapplogin.back.internal.DisposableMessage
import com.infomaniak.core.crossapplogin.back.internal.certificates.AppCertificateChecker
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdManager
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdResync
import com.infomaniak.core.crossapplogin.back.internal.localAccountsFlow
import com.infomaniak.core.sentry.SentryLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class BaseCrossAppLoginService(protected open val selectedUserIdFlow: Flow<Int?>) : LifecycleService() {

    constructor() : this(selectedUserIdFlow = emptyFlow())

    private val incomingMessagesChannel = Channel<DisposableMessage>(capacity = Channel.UNLIMITED)
    private val messagesHandler = ChannelMessageHandler(incomingMessagesChannel)
    private val messenger by lazy { Messenger(messagesHandler) }

    private val certificateChecker = AppCertificateChecker.withInfomaniakApps

    private val signedInAccountRequests = Channel<Messenger>(capacity = Channel.UNLIMITED)
    private val syncSharedDeviceIdRequests = Channel<ByteArray>(capacity = Channel.UNLIMITED)

    private val crossAppLogin by lazy { CrossAppLogin.forContext(this, lifecycleScope) }

    init {
        lifecycleScope.launch {
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.STARTED) }
            launch { handleIncomingMessages() }
            handleSignedInAccountsRequests(selectedUserIdFlow)
        }
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
                handleMessageFromAllowedSource(msg)
            }
        }.onCompletion {
            messagesHandler.removeCallbacksAndMessages(null)
        }.collect()
    }

    private fun CoroutineScope.handleMessageFromAllowedSource(msg: Message) {
        val trustedClientMessenger = msg.replyTo
        val what = IpcMessageWhat.entries.getOrNull(msg.what) ?: return
        try {
            when (what) {
                IpcMessageWhat.GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS -> {
                    check(signedInAccountRequests.trySend(trustedClientMessenger).isSuccess)
                }
                IpcMessageWhat.GET_SHARED_DEVICE_ID -> {
                    val requestData = msg.extractProtoEncodedData<CrossAppDeviceIdRequest>()
                    launch { returnSharedDeviceIdOrWaitForSync(trustedClientMessenger, requestData) }
                }
                IpcMessageWhat.SYNC_SHARED_DEVICE_ID -> syncSharedDeviceIdRequests.trySend(msg.unwrapByteArrayOrNull()!!)
                IpcMessageWhat.RESYNC_SHARED_DEVICE_ID_REQUEST -> {
                    val request = msg.extractProtoEncodedData<SharedDeviceIdResync.ResyncRequest>()
                    launch {
                        crossAppLogin.sharedDeviceIdResync.handleSharedDeviceIdResyncRequest(trustedClientMessenger, request)
                    }
                }
                IpcMessageWhat.RESYNC_SHARED_DEVICE_ID_REPORT -> {
                    TODO()
                }
            }
        } catch (e: Exception) {
            when (e) {
                is SerializationException, is IllegalArgumentException, is NullPointerException -> {
                    SentryLog.wtf(TAG, "${what.name} message didn't contain expected data", e)
                }
            }
        }
    }

    @Throws(NullPointerException::class, SerializationException::class, IllegalArgumentException::class)
    private inline fun <reified T> Message.extractProtoEncodedData(): T {
        val data = unwrapByteArrayOrNull()!!
        return ProtoBuf.decodeFromByteArray<T>(data)
    }

    private suspend fun handleSignedInAccountsRequests(selectedUserIdFlow: Flow<Int?>) = Dispatchers.Default {
        val accountsDataFlow: SharedFlow<ByteArray> = localAccountsFlow(selectedUserIdFlow)
            .map { ProtoBuf.encodeToByteArray(it) }
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
        val sharedDeviceIdManager = crossAppLogin.sharedDeviceIdManager

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

    /**
     * ## Important message to editors
     *
     * For all existing entries, unless the given ones never made it to a public release (which includes betas!):
     * - **NEVER** change their order.
     * - **NEVER** remove one that was present before.
     * - **NEVER** reuse one that was used before. Those shall be unique and stable, essentially forever.
     * - **Safe changes:**
     *    1. Renaming any existing entry (without changing its purpose)
     *    2. Adding new entries AT THE END.
     *    3. Deprecating any entry (without changing its order).
     *    4. Renaming this enum class.
     */
    internal enum class IpcMessageWhat {
        GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS,
        GET_SHARED_DEVICE_ID,
        SYNC_SHARED_DEVICE_ID,
        RESYNC_SHARED_DEVICE_ID_REQUEST,
        RESYNC_SHARED_DEVICE_ID_REPORT,
    }

    companion object {
        const val ACTION = "com.infomaniak.crossapp.login"
        private const val TAG = "BaseCrossAppLoginService"
    }
}
