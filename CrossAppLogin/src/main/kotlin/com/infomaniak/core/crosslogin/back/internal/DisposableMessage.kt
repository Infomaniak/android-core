/*
 * Infomaniak Core - Android
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

@file:OptIn(ExperimentalContracts::class)

package com.infomaniak.core.crosslogin.back.internal

import android.os.Message
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
internal value class DisposableMessage private constructor(private val message: Message) {

    companion object {
        fun fromCopy(
            originalMessage: Message,
            recycleOriginalMessage: Boolean
        ): DisposableMessage {
            val copiedMessage = Message.obtain(originalMessage)
            if (recycleOriginalMessage) originalMessage.recycle()
            return DisposableMessage(copiedMessage)
        }
    }

    inline fun <R> use(block: (msg: Message) -> R): R {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        return try {
            block(message)
        } finally {
            message.recycle()
        }
    }
}
