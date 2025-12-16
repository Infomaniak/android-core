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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.runner.RunWith
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
@Ignore("Doesn't work on all machines, but seems to run consistently on M4 Max CPUs…")
class RateLimitTest {

    // NOTE: We are NOT using runTest below because built-in delay skipping is not connected to System.nanoTime()

    @Test
    fun `First before limit, and last after, make it through`() = runBlocking {
        val result = flow {
            for (i in 1..3) {
                emit(i)
                delay(5.milliseconds)
            }
            repeat(4) {
                emit(4)
            }
            emit(5)
        }.rateLimit(30.milliseconds, elementsBeforeLimit = 2).toList()
        assertEquals(expected = listOf(1, 2, 5), result)
    }

    @Test
    fun `Rate limit is lifted after interval`() = runBlocking {
        val result = flow {
            for (i in 1..3) emit(i) // First 2 elements should be sent.
            emit(4) // Should be sent AFTER the rate limiting
            delay(30.milliseconds)
            emit(5) // Second element since last rate limiting
            emit(6) // Should hit the limit
            emit(7) // Should eventually be sent
            delay(30.milliseconds)
            emit(8)
        }.onEach { yield() }.rateLimit(30.milliseconds, elementsBeforeLimit = 2).toList()
        assertEquals(expected = listOf(1, 2, 4, 5, 7, 8), result)
    }

    @Test
    fun `Rate limit is enforced before interval`() = runBlocking {
        val result = flow {
            for (i in 1..3) emit(i) // First 2 elements should be sent.
            emit(4)
            delay(20.milliseconds)
            emit(5) // Should replace the previous element
            delay(10.milliseconds)
            emit(6) // Second element since last rate limiting
            emit(7) // Should hit the limit
            emit(8) // Should eventually be sent
            delay(30.milliseconds)
            emit(9)
        }.onEach { yield() }.rateLimit(30.milliseconds, elementsBeforeLimit = 2).toList()
        assertEquals(expected = listOf(1, 2, 5, 6, 8, 9), result)
    }

    @Test
    fun `10 elements with one extra each time`() = runBlocking {
        val result = flow {
            repeat(10) {
                emit(it + 1)
                delay(10.milliseconds)
            }
        }.rateLimit(30.milliseconds, elementsBeforeLimit = 2).toList()
        assertEquals(expected = listOf(1, 2, 4, 5, 7, 8, 10), result)
    }

    @Test
    fun `100 elements with one extra each time`() = runBlocking {
        val count = 100
        val result = flow {
            repeat(count) {
                emit(it + 1)
                delay(2.milliseconds)
            }
        }.rateLimit(6.milliseconds, elementsBeforeLimit = 2).toList()
        assertEquals(expected = (0..count).filterNot { it % 3 == 0 }, result)
    }

    private fun <T> Flow<T>.rateLimit(minInterval: Duration, elementsBeforeLimit: Int): Flow<T> {
        return rateLimit(minInterval, elementsBeforeLimit) { System.nanoTime() }
    }
}
