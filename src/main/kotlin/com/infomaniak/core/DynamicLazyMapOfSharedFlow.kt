/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
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

import com.infomaniak.core.DynamicLazyMap.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * Helper to create a [DynamicLazyMap] of [SharedFlow]s with a [Flow] factory.
 *
 * @see flowForKey
 */
fun <K, E> DynamicLazyMap.Companion.sharedFlow(
    cacheManager: CacheManager<K, SharedFlow<E>>? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    createFlow: CoroutineScope.(K) -> Flow<E>,
): DynamicLazyMap<K, SharedFlow<E>> = DynamicLazyMap<K, SharedFlow<E>>(
    cacheManager = cacheManager,
    coroutineScope = coroutineScope,
    createElement = { key -> createFlow(key).shareIn(this, SharingStarted.Lazily, replay = 1) }
)

/**
 * Helper to directly get a [Flow] from a [DynamicLazyMap] containing [SharedFlow]s.
 *
 * @see DynamicLazyMap.Companion.sharedFlow
 */
fun <K, E> DynamicLazyMap<K, SharedFlow<E>>.flowForKey(key: K): Flow<E> = flow {
    useElement(key) { sharedFlow: SharedFlow<E> ->
        emitAll(sharedFlow)
    }
}
