/*
 * Infomaniak SwissTransfer - Android
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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.infomaniak.core

import android.os.SystemClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

suspend fun <R> Flow<Boolean>.tryCompletingWhileTrue(
    block: suspend () -> R
): R = transformLatest {
    if (it) emit(block())
}.first()

fun <T> Flow<T>.rateLimit(minInterval: Duration): Flow<T> = channelFlow {
    var lastEmitElapsedNanos = 0L
    collectLatest { newValue ->
        val nanosSinceLastEmit = SystemClock.elapsedRealtimeNanos() - lastEmitElapsedNanos
        val timeSinceLastEmit = nanosSinceLastEmit.nanoseconds
        val timeToWait = minInterval - timeSinceLastEmit.coerceAtMost(minInterval)
        delay(timeToWait)
        // We use `sendAtomic` instead of just `send` to ensure `lastEmitElapsedNanos` is updated
        // if the value was successfully sent while cancellation was happening.
        // That ensures we count every value being sent, and respect the desired rate limiting.
        // FYI, as its documentation states, `send` can deliver the value to the receiver,
        // and throw a `CancellationException` instead of returning (if coroutine's `Job` gets cancelled).
        sendAtomic(newValue)
        lastEmitElapsedNanos = SystemClock.elapsedRealtimeNanos()
    }
}.buffer(Channel.RENDEZVOUS)
