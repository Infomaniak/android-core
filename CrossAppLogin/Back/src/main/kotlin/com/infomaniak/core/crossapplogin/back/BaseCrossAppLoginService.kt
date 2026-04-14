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
import android.os.DeadObjectException
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Process
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.crossapplogin.back.internal.ChannelMessageHandler
import com.infomaniak.core.crossapplogin.back.internal.DisposableMessage
import com.infomaniak.core.crossapplogin.back.internal.certificates.AppCertificateChecker
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdManager
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdResync
import com.infomaniak.core.crossapplogin.back.internal.localAccountsFlow
import com.infomaniak.core.sentry.SentryLog
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

                val trustedClientMessenger = msg.replyTo
                when (IpcMessageWhat.entries.getOrNull(msg.what)) {
                    null -> Unit
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
                    IpcMessageWhat.RESYNC_SHARED_DEVICE_ID_REQUEST -> {
                        val request = runCatching {
                            ProtoBuf.decodeFromByteArray<SharedDeviceIdResync.ResyncRequest>(msg.unwrapByteArrayOrNull()!!)
                        }.getOrElse { t ->
                            SentryLog.wtf(TAG, "RESYNC_SHARED_DEVICE_ID_REQUEST message didn't contain expected data", t)
                            return@use
                        }
                        handleSharedDeviceIdResyncRequest(trustedClientMessenger, request)
                    }
                    IpcMessageWhat.RESYNC_SHARED_DEVICE_ID_REPORT -> {
                        TODO()
                    }
                }
            }
        }.onCompletion {
            messagesHandler.removeCallbacksAndMessages(null)
        }.collect()
    }

    private suspend fun handleSignedInAccountsRequests(selectedUserIdFlow: Flow<Int?>) = Dispatchers.Default {
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

    private suspend fun handleSharedDeviceIdResyncRequest(
        trustedClientMessenger: Messenger,
        request: SharedDeviceIdResync.ResyncRequest
    ) {
        val counterRequest = crossAppLogin.sharedDeviceIdResync.onExternalResyncIncoming(request)
        val response: SharedDeviceIdResync.ResyncResponse = if (counterRequest != null) {
            SharedDeviceIdResync.ResyncResponse.AlreadySyncing(counterRequest)
        } else {
            TODO()
        }
        TODO("Reply with response")
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
