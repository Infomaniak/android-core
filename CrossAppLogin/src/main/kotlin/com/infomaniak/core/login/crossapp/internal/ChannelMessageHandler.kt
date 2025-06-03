/*
 * Infomaniak Login - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
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

package com.infomaniak.core.login.crossapp.internal

import android.os.Handler
import android.os.Message
import kotlinx.coroutines.channels.SendChannel
import splitties.mainthread.mainLooper
import java.lang.ref.WeakReference

/**
 * A Handler that sends everything it receives to the passed [channel], as a copy.
 * The channel is held in a WeakReference to avoid Handler leak.
 *
 * Used for IPC (inter-process communication)
 */
internal class ChannelMessageHandler(channel: SendChannel<DisposableMessage>) : Handler(mainLooper) {

    private val weakChannelReference = WeakReference(channel)

    override fun handleMessage(msg: Message) {
        weakChannelReference.get()?.let { channel ->
            val disposableMessage = DisposableMessage.Companion.fromCopy(msg, recycleOriginalMessage = true)
            channel.trySend(disposableMessage)
        }
    }
}
