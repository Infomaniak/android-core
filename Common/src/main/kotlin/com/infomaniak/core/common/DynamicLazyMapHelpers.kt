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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * Helper to create a [DynamicLazyMap] of [SharedFlow]s with a [Flow] factory.
 *
 * It's equivalent to [dynamicLazyMapOfSharedFlow].
 *
 * @see flowForKey
 */
fun <K, E> DynamicLazyMap.Companion.sharedFlow(
    cacheManager: DynamicLazyMap.CacheManager<K, SharedFlow<E>>? = null,
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
 * @see sharedFlow
 */
fun <K, E> DynamicLazyMap<K, SharedFlow<E>>.flowForKey(key: K): Flow<E> = flow {
    useElement(key) { sharedFlow: SharedFlow<E> ->
        emitAll(sharedFlow)
    }
}

/**
 * Creates a [DynamicLazyMap].
 *
 * @see DynamicLazyMap
 */
fun <K, E> CoroutineScope.dynamicLazyMap(
    cacheManager: DynamicLazyMap.CacheManager<K, E>? = null,
    createElement: CoroutineScope.(K) -> E
): DynamicLazyMap<K, E> {
    return DynamicLazyMap(
        cacheManager = cacheManager,
        coroutineScope = this,
        createElement = createElement
    )
}

/**
 * Helper to create a [DynamicLazyMap] of [SharedFlow]s with a [Flow] factory.
 *
 * It's equivalent to [sharedFlow].
 *
 * @see flowForKey
 */
fun <K, E> CoroutineScope.dynamicLazyMapOfSharedFlow(
    cacheManager: DynamicLazyMap.CacheManager<K, SharedFlow<E>>? = null,
    createFlow: CoroutineScope.(K) -> Flow<E>,
): DynamicLazyMap<K, SharedFlow<E>> {
    return DynamicLazyMap.sharedFlow(
        cacheManager = cacheManager,
        coroutineScope = this,
        createFlow = createFlow
    )
}

inline fun <K, reified E, R> DynamicLazyMap<K, SharedFlow<E>>.combineFor(
    keys: Set<K>,
    crossinline transform: suspend (Array<E>) -> R
): Flow<R> = flow {
    useElements(keys) { emitAll(combine(it.values, transform)) }
}

typealias UseElementSuspend<K, E, R> = suspend (K, suspend (E) -> R) -> R

fun <K, E, R> DynamicLazyMap<K, E>.asFunction(): UseElementSuspend<K, E, R> {
    return { key: K, block: suspend (E) -> R ->
        useElement(key) { element ->
            block(element)
        }
    }
}

context(scope: CoroutineScope)
fun <K, E> UseElementSuspend<K, E, Nothing>.toDynamicLazyMap(): DynamicLazyMap<K, E> {
    return scope.dynamicLazyMap(createElement = asDynamicLazyMapCreateElement())
}

private fun <K, E> UseElementSuspend<K, E, Nothing>.asDynamicLazyMapCreateElement(): CoroutineScope.(K) -> E {
    val useElement: UseElementSuspend<K, E, Nothing> = this
    return fun CoroutineScope.(key: K): E {
        var element: E? = null
        launch(start = CoroutineStart.UNDISPATCHED) {
            useElement(key) {
                element = it
                awaitCancellation()
            }
        }
        @Suppress("UNCHECKED_CAST")
        return element as E
    }
}
