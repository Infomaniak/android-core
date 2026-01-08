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
package com.infomaniak.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * [SendChannel.send] can throw a [CancellationException] after the value was sent,
 * which might not be desirable if we want to factor-in whether the value was actually sent.
 *
 * That's why this atomic version exists.
 * It supports cancellation, but if the value is sent, it will return instead, even if it's
 * happening concurrently to cancellation.
 *
 * See [this issue](https://github.com/Kotlin/kotlinx.coroutines/issues/4414) for more details.
 */
suspend fun <T> SendChannel<T>.sendAtomic(element: T) {
    trySelectAtomically<Unit?>(onCancellation = { null }) {
        onSend(element) {}
    } ?: currentCoroutineContext().ensureActive()
}
