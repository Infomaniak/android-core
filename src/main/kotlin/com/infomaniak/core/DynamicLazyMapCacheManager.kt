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

package com.infomaniak.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import splitties.experimental.ExperimentalSplittiesApi
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

fun <K, E> DynamicLazyMap.CacheManager.Companion.maxElements(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    maxUnusedElementsCount: Int
): DynamicLazyMap.CacheManager<K, E> {
    return unusedCountBased(
        coroutineScope = coroutineScope,
        handleUnusedElements = { unusedCount ->
            unusedCount.collect { unusedElementsCount ->
                val aboveTheLimitCount = unusedElementsCount - maxUnusedElementsCount
                if (aboveTheLimitCount > 0) dropUnusedElements(aboveTheLimitCount)
            }
        }
    )
}

fun <K, E> DynamicLazyMap.CacheManager.Companion.unusedCountBased(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    handleUnusedElements: suspend CacheExpirationScope.(unusedCount: StateFlow<Int>) -> Nothing
): DynamicLazyMap.CacheManager<K, E> {
    return DynamicLazyMapCacheManager<K, E>(
        coroutineScope = coroutineScope,
        handleUnusedElements = handleUnusedElements
    )
}

interface CacheExpirationScope {
    suspend fun dropUnusedElements(count: Int)
}

private class DynamicLazyMapCacheManager<K, E>(
    @Suppress("can_be_parameter") // To avoid this issue: https://github.com/Kotlin/kotlinx.coroutines/issues/1061
    private val coroutineScope: CoroutineScope,
    handleUnusedElements: suspend CacheExpirationScope.(unusedElements: StateFlow<Int>) -> Nothing
) : DynamicLazyMap.CacheManager<K, E>, CacheExpirationScope {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun DynamicLazyMap<K, E>.waitForCacheExpiration(key: K, element: E) {
        if (dynamicLazyMapAsync.isCompleted) {
            check(this === dynamicLazyMapAsync.getCompleted()) {
                "A cacheManager instance should NOT be shared among multiple DynamicLazyMap instances"
            }
        } else {
            dynamicLazyMapAsync.complete(this)
        }
        suspendCancellableCoroutine { continuation ->
            mutationLock.withLock { unusedElements.add(continuation) }
            continuation.invokeOnCancellation { mutationLock.withLock { unusedElements.remove(continuation) } }
        }
    }

    private val dynamicLazyMapAsync = CompletableDeferred<DynamicLazyMap<K, E>>()

    private val unusedElements = ArrayDeque<Continuation<Unit>>()
    private val mutationLock = ReentrantLock()

    init {
        coroutineScope.launch {
            val dynamicLazyMap = dynamicLazyMapAsync.await()
            handleUnusedElements(dynamicLazyMap.cachedElementsCount)
        }
    }

    override suspend fun dropUnusedElements(count: Int) {
        withContext(NonCancellable) {
            // We need to let any freshly launched coroutine that calls `waitForCacheExpiration`
            // to have the time to reach suspendCancellableCoroutine,
            // where its continuation is put inside `unusedElements`.
            // Otherwise, we arrive too early and could remove nothing.
            yield()
        }
        mutationLock.withLock { repeat(count) { unusedElements.removeFirstOrNull()?.resume(Unit) } }
    }
}
