/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2025 Infomaniak Network SA
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
package com.infomaniak.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class SendAtomicTest {

    @Test
    fun `sendAtomic return after each successful sending`() = runBlocking {
        // We are NOT using runTest here to match the equivalent with `sendAtomicBroken`
        val source = flowOf(1, 2, 3).onEach { delay(2.milliseconds) }
        var returnCount = 0
        val elementsSent = mutableListOf<Int>()
        val result = channelFlow {
            source.collectLatest {
                sendAtomic(it)
                elementsSent += it
                returnCount++
            }
        }.buffer(capacity = Channel.RENDEZVOUS).toList()
        assertEquals(expected = listOf(1, 2, 3), result)
        assertEquals(expected = listOf(1, 2, 3), elementsSent)
        assertEquals(expected = 3, returnCount)
    }

    @Test
    fun `sendAtomicBroken doesn't always return after successfully sending`() = runBlocking {
        // We are NOT using runTest here because delay skipping affects the test behavior,
        // and the bug we're testing doesn't reproduce that way.
        val source = flowOf(1, 2, 3).onEach {
            delay(2.milliseconds) // Intentional delay to not trigger collectLatest immediately.
        }
        var returnCount = 0
        val elementsSent = mutableListOf<Int>()
        val result = channelFlow {
            source.collectLatest { // Note that the bug doesn't reproduce with a regular collect.
                runCatching { sendAtomicBroken(it) }.onFailure { t ->
                    t.printStackTrace() // NOTE: THERE IS something thrown (see below), which gets silently eaten
                    // by collectLatest, and this likely plays a part in our bug.
                    // What gets thrown by `sendAtomicBroken`:
                    // kotlinx.coroutines.flow.internal.ChildCancelledException: Child of the scoped flow was cancelled
                }.getOrThrow()
                elementsSent += it
                returnCount++
            }
        }.buffer(capacity = Channel.RENDEZVOUS).toList()
        assertEquals(expected = listOf(1, 2, 3), result)
        assertEquals(expected = listOf(2, 3), elementsSent) // This is broken
        assertEquals(expected = 2, returnCount) // This is broken
    }
}

suspend fun <T> SendChannel<T>.sendAtomicBroken(element: T) {
    trySelectAtomicallyBroken<Unit?>(onCancellation = { null }) {
        onSend(element) { }
    } ?: currentCoroutineContext().ensureActive()
}

/**
 * This is similar to [trySelectAtomically], but using `launch { awaitCancellation() }` instead of `Job(parent = …)`,
 * which should work the same, but doesn't, for some yet to discover reason.
 */
private suspend inline fun <R> trySelectAtomicallyBroken(
    crossinline onCancellation: suspend () -> R,
    crossinline builder: SelectBuilder<R>.() -> Unit
): R? = coroutineScope {
    // We connect `cancellationSignal` to the current job, so it can be used as
    // a secondary select clause below, even though cancellation is blocked
    // using `withContext(NonCancellable)`.
    // The atomic behavior of `select` allows us to get the desired behavior.
    val cancellationSignal = launch { awaitCancellation() } // This is what is breaking it. Commented line below would fix it.
    // val cancellationSignal = Job(parent = currentCoroutineContext().job)
    withContext(NonCancellable) {
        select {
            builder() // We need to be biased towards this/these clause(s), so it comes first.
            cancellationSignal.onJoin { onCancellation() }
        }.also {
            // If a builder clause was selected, stop this job to allow coroutineScope to complete.
            cancellationSignal.cancel()
        }
    }
}
