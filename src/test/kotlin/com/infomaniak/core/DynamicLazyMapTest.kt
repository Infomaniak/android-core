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
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DynamicLazyMapTest {

    @Test
    fun `Launched coroutines are cleaned up on cache expiration`() = runTest {
        var isRunning = false
        val map = DynamicLazyMap<String, Unit>(coroutineScope = this) {
            launch {
                try {
                    isRunning = true
                    awaitCancellation()
                } finally {
                    isRunning = false
                }
            }
            Unit
        }
        assertFalse(isRunning)
        yield()
        assertFalse(isRunning)
        map.useElement(key = "") {
            yield()
            assertTrue(isRunning)
        }
        yield()
        assertFalse(isRunning)
    }

    @Test
    fun `Contains the correct elements`() = runTest {
        val numberOfElements = 12
        val map = DynamicLazyMap<UInt, String>(coroutineScope = this, createElement = { it.toString() })
        val keys = buildSet(numberOfElements) { repeat(numberOfElements) { add(it.toUInt()) } }

        val wasExecuted: Boolean
        map.useElements(keys) { elementsMap ->
            assertEquals(expected = numberOfElements, elementsMap.size)
            elementsMap.forEach { (key, value) ->
                assertEquals(key.toUInt(), value.toUInt())
            }
            wasExecuted = true
        }
        @Suppress("KotlinConstantConditions") // We're testing the contract is honored.
        assertTrue(wasExecuted)
    }

    @Test
    fun `Size matches used elements`() = runTest {
        repeat(3) {
            testSizeMatchesUsedElements(numberOfElements = it)
        }
        testSizeMatchesUsedElements(numberOfElements = 30)
    }

    private suspend fun testSizeMatchesUsedElements(numberOfElements: Int) = coroutineScope {
        val map = DynamicLazyMap<String, Boolean>(coroutineScope = this, createElement = { Random.nextBoolean() })
        val keys = buildSet(numberOfElements) { repeat(numberOfElements) { add("$it") } }
        val usedElements by map.usedElementsCount::value

        // Test with useElements
        assertEquals(expected = 0, actual = usedElements)
        map.useElements(keys) {
            assertEquals(expected = numberOfElements, actual = usedElements)
        }
        assertEquals(expected = 0, actual = usedElements)

        if (numberOfElements > 1) {
            // Test with combination of useElement and useElements
            map.useElements(keys.asSequence().drop(1).toSet()) {
                assertEquals(expected = numberOfElements - 1, actual = usedElements)
                map.useElement(keys.first()) {
                    assertEquals(expected = numberOfElements, actual = usedElements)
                }
                assertEquals(expected = numberOfElements - 1, actual = usedElements)
            }
            assertEquals(expected = 0, actual = usedElements)
            // Test multiple concurrent calls to useElement
            val elementUsingJobs = launch(start = CoroutineStart.UNDISPATCHED) {
                keys.forEach { key ->
                    launch(start = CoroutineStart.UNDISPATCHED) {
                        map.useElement(key) { awaitCancellation() }
                    }
                }
            }
            assertEquals(expected = numberOfElements, actual = usedElements)
            elementUsingJobs.cancelAndJoin()
            assertEquals(expected = 0, actual = usedElements)
        }
    }

    //TODO: Test elements are shared and are not created over and over.
    //TODO: Test values totalElements and unusedElements
}
