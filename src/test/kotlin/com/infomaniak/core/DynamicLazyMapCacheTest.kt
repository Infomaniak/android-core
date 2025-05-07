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
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class DynamicLazyMapCacheTest {

    @Test
    fun `maxElements should cache and cleanup as needed`() = runTest {
        autoCancelScope {
            var createElementCallCount = 0
            val map = DynamicLazyMap<Int, String>(
                cacheManager = DynamicLazyMap.CacheManager.maxElements(
                    coroutineScope = this,
                    maxUnusedElementsCount = 2
                ),
                coroutineScope = this,
                createElement = { key ->
                    createElementCallCount++
                    key.toString()
                }
            )
            createElementCallCount shouldBe 0
            coroutineScope {
                map.useElement(1) {
                    createElementCallCount shouldBe 1
                    launch {
                        map.useElement(1) {
                            createElementCallCount shouldBe 1
                        }
                    }
                }
            }
            map.useElement(2) {
                createElementCallCount shouldBe 2
            }
            map.useElement(1) {
                createElementCallCount shouldBe 2
            }
            map.useElement(2) {
                createElementCallCount shouldBe 2
            }
            map.useElement(3) {
                createElementCallCount shouldBe 3
            }
            map.useElement(2) {
                createElementCallCount shouldBe 3
            }
            map.useElement(3) {
                createElementCallCount shouldBe 3
            }
            map.useElement(4) {
                createElementCallCount shouldBe 4
            }
            repeat(3) { yield() }
            map.cachedElementsCount.value shouldBe 2
            map.useElement(1) {
                createElementCallCount shouldBe 5
            }
            map.useElement(4) {
                createElementCallCount shouldBe 5
            }
            repeat(3) { yield() }
            map.useElement(3) {
                createElementCallCount shouldBe 6
            }
        }
    }
}
