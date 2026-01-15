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
@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.StateFlow
import splitties.experimental.ExperimentalSplittiesApi

fun <K, E> DynamicLazyMap.CacheManager.Companion.maxElements(
    maxCacheSize: Int,
    waitForElementExpiration: suspend DynamicLazyMap<K, E>.(key: K, element: E) -> Unit = { _, _ ->
        awaitCancellation()
    },
): DynamicLazyMap.CacheManager<K, E> = object : DynamicLazyMap.CacheManager<K, E> {

    override fun onUnused(
        key: K,
        element: E,
        currentCacheSize: Int,
        usedElementsCount: Int
    ) = DynamicLazyMap.OnUnusedBehavior(
        cacheUntilExpired = true,
        evictOldest = currentCacheSize >= maxCacheSize
    )

    override suspend fun DynamicLazyMap<K, E>.waitForCacheExpiration(key: K, element: E) {
        waitForElementExpiration(key, element)
    }
}

@NotTestedNorUsed
fun <K, E> DynamicLazyMap.CacheManager.Companion.unusedCountBased(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    handleUnusedElements: suspend CacheExpirationScope.(unusedCount: StateFlow<Int>) -> Nothing
): DynamicLazyMap.CacheManager<K, E> {
    return DynamicLazyMapCacheManager(
        coroutineScope = coroutineScope,
        handleUnusedElements = handleUnusedElements
    )
}
