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

import androidx.collection.mutableIntListOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class DynamicLazyMapCacheTest {

    @Test
    fun `maxElements should cache until maxSize`() = runTest {
        autoCancelScope {
            var createElementCallCount = 0
            val maxCacheSize = 8
            val map = DynamicLazyMap<Int, String>(
                cacheManager = DynamicLazyMap.CacheManager.maxElements(maxCacheSize = maxCacheSize),
                coroutineScope = this,
                createElement = { key ->
                    createElementCallCount++
                    key.toString()
                }
            )
            var expectedCreateElementCallCount = 0

            // Fill the cache and check createElement was called the right number of times
            repeat(maxCacheSize) { index ->
                expectedCreateElementCallCount++
                map.useElement(key = index) { createElementCallCount shouldBe expectedCreateElementCallCount }
                createElementCallCount shouldBe expectedCreateElementCallCount
            }

            // Same elements should be retrieved from the cache
            repeat(maxCacheSize) { index -> map.useElement(key = index) {} }
            createElementCallCount shouldBe expectedCreateElementCallCount

            val extraElements = 11
            repeat(maxCacheSize + extraElements) { index -> map.useElement(key = index) {} }
            expectedCreateElementCallCount += extraElements
            createElementCallCount shouldBe expectedCreateElementCallCount
        }
    }

    @Test
    fun `maxElements should cleanup cache beyond maxSize`() = runTest {
        autoCancelScope {
            var createElementCallCount = 0
            val cancelledJobs = mutableIntListOf()
            val maxCacheSize = 8
            val map = DynamicLazyMap<Int, String>(
                cacheManager = DynamicLazyMap.CacheManager.maxElements(maxCacheSize = maxCacheSize),
                coroutineScope = this,
                createElement = { key ->
                    createElementCallCount++
                    coroutineContext.job.invokeOnCompletion { if (it != null) cancelledJobs.add(key) }
                    key.toString()
                }
            )

            // Fill the cache
            var expectedCreateElementCallCount = maxCacheSize
            repeat(maxCacheSize) { index -> map.useElement(key = index) {} }
            createElementCallCount shouldBe expectedCreateElementCallCount

            // Same elements should be retrieved from the cache
            repeat(maxCacheSize) { index -> map.useElement(key = index) {} }
            createElementCallCount shouldBe expectedCreateElementCallCount

            // All previously cached values should be evicted as we request values for different keys with the max cache size.
            for (index in maxCacheSize until maxCacheSize * 2) map.useElement(key = index) {}
            expectedCreateElementCallCount += maxCacheSize
            createElementCallCount shouldBe expectedCreateElementCallCount

            // The given scopes shall be cancelled.
            cancelledJobs.size shouldBe maxCacheSize
            repeat(maxCacheSize) { index -> (index in cancelledJobs).shouldBeTrue() }
        }
    }

    @Test
    fun `maxElements should cache and cleanup as needed`() = runTest {
        autoCancelScope {
            var createElementCallCount = 0
            val maxCacheSize = 2
            val map = DynamicLazyMap<Int, String>(
                cacheManager = DynamicLazyMap.CacheManager.maxElements(maxCacheSize = maxCacheSize),
                coroutineScope = this,
                createElement = { key ->
                    createElementCallCount++
                    key.toString()
                }
            )
            val cacheSize by map.cachedElementsCount::value
            val totalElements by map.totalElementsCount::value
            createElementCallCount shouldBe 0
            totalElements shouldBe 0
            coroutineScope {
                map.useElement(1) {
                    createElementCallCount shouldBe 1
                    totalElements shouldBe 1
                    launch {
                        map.useElement(1) {
                            createElementCallCount shouldBe 1
                            totalElements shouldBe 1
                        }
                    }
                }
            }
            cacheSize shouldBe 1.coerceAtMost(maxCacheSize)
            totalElements shouldBe 1
            map.useElement(2) {
                createElementCallCount shouldBe 2
                totalElements shouldBe 2
            }
            cacheSize shouldBe 2.coerceAtMost(maxCacheSize)
            map.useElement(1) {
                cacheSize shouldBe 1.coerceAtMost(maxCacheSize)
                createElementCallCount shouldBe 2
            }
            cacheSize shouldBe 2.coerceAtMost(maxCacheSize)
            map.useElement(2) {
                cacheSize shouldBe 1.coerceAtMost(maxCacheSize)
                createElementCallCount shouldBe 2
            }
            cacheSize shouldBe 2.coerceAtMost(maxCacheSize)
            map.useElement(3) {
                cacheSize shouldBe 2.coerceAtMost(maxCacheSize)
                createElementCallCount shouldBe 3
            }
            cacheSize shouldBe 3.coerceAtMost(maxCacheSize)
            map.useElement(2) { createElementCallCount shouldBe 3 }
            map.useElement(3) { createElementCallCount shouldBe 3 }
            map.useElement(4) { createElementCallCount shouldBe 4 }
            cacheSize shouldBe maxCacheSize
            map.useElement(1) { createElementCallCount shouldBe 5 }
            map.useElement(4) { createElementCallCount shouldBe 5 }
            map.useElement(3) { createElementCallCount shouldBe 6 }
            cacheSize shouldBe maxCacheSize
        }
    }

    @Test
    fun `Immediate cleanup if CacheManager#onUnusedElement says to not cache nor to evict oldest element`() = runTest {
        val concurrentElements = 3
        val concurrentUsagesPerElement = 3
        require(concurrentUsagesPerElement > 1)

        val cacheManager = object : DynamicLazyMap.CacheManager<Int, String> {

            override fun onUnused(
                key: Int,
                element: String,
                currentCacheSize: Int,
                usedElementsCount: Int
            ) = DynamicLazyMap.OnUnusedBehavior(
                cacheUntilExpired = false,
                evictOldest = false
            )

            override suspend fun DynamicLazyMap<Int, String>.waitForCacheExpiration(
                key: Int,
                element: String
            ) = Unit
        }

        val createElementCallsCountSummary = buildMap(capacity = concurrentElements) {
            for (elementKey in 1..concurrentElements) this[elementKey] = AtomicInteger()
        }

        val map = DynamicLazyMap<Int, String>(
            coroutineScope = this,
            cacheManager = cacheManager,
            createElement = { key ->
                createElementCallsCountSummary.getValue(key).incrementAndGet()
                key.toString()
            }
        )
        val cacheSize by map.cachedElementsCount::value
        val usedElements by map.usedElementsCount::value
        val totalElements by map.totalElementsCount::value

        // Test multiple concurrent calls to useElement
        coroutineScope {
            for (elementKey in 1..concurrentElements) repeat(concurrentUsagesPerElement) {
                launch { map.useElement(elementKey) { delay(1.seconds) } }
            }
        }
        createElementCallsCountSummary.forEach { (_, createElementCallsCount) ->
            createElementCallsCount.get() shouldBe 1
        }
        cacheSize shouldBe 0
        usedElements shouldBe 0
        totalElements shouldBe 0

        // Test with concurrent calls to useElements
        coroutineScope {
            val keys = buildSet(concurrentElements) {
                for (elementKey in 1..concurrentElements) add(elementKey)
            }
            repeat(concurrentUsagesPerElement) {
                launch { map.useElements(keys) { delay(1.seconds) } }
            }
        }
        createElementCallsCountSummary.forEach { (_, createElementCallsCount) ->
            createElementCallsCount.get() shouldBe 2
        }
        cacheSize shouldBe 0
        usedElements shouldBe 0
        totalElements shouldBe 0
    }
}
