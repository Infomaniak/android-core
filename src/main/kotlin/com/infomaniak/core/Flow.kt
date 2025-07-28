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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.infomaniak.core

import android.os.Process
import android.os.SystemClock
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest
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

/**
 * Every time the resulting flow is collected, it will create a new internal [android.os.HandlerThread],
 * and when the flow is cancelled or completes, this thread will be quit (with [android.os.HandlerThread.quitSafely]).
 *
 * This is helpful when you need a given flow to be activated on a single, looper thread.
 *
 * Example usage:
 * ```
 * val someDataFlow: Flow<Something> = flow {
 *     getSomeDatabaseInstance().use { db ->
 *         val flow = db.someQuery()
 *             .toFlow()
 *             .map { it?.toSomethingElse() }
 *         emitAll(flow)
 *     }
 * }.flowOnNewHandlerThread(name = "someData")
 * ```
 *
 * Note that if [name] is left to `null`, the system will generate a default thread name.
 *
 * Also note that the JVM and ART allow multiple threads to have the same name.
 * (Just keep it in mind if there's a need to analyze which thread is doing what.)
 */
fun <T> Flow<T>.flowOnNewHandlerThread(
    name: String? = null,
    priority: Int = Process.THREAD_PRIORITY_DEFAULT
): Flow<T> = flowOnLazyClosable {
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    HandlerThreadDispatcher(name = name, priority = priority)
}

/**
 * Similar to [flowOn], but will lazily create the Dispatcher returned by
 * [createDispatcher], and will close it once the flow is cancelled or completes.
 *
 * Example usage:
 * ```
 * val someDataFlow: Flow<Something> = flow {
 *     getSomeDatabaseInstance().use { db ->
 *         val flow = db.someQuery()
 *             .toFlow()
 *             .map { it?.toSomethingElse() }
 *         emitAll(flow)
 *     }
 * }.flowOnLazyClosable {
 *     newSingleThreadContext("someData")
 * }
 * ```
 */
fun <T> Flow<T>.flowOnLazyClosable(
    createDispatcher: () -> CloseableCoroutineDispatcher
): Flow<T> = flow {
    createDispatcher().use { dispatcher ->
        emitAll(flowOn(dispatcher))
    }
}
