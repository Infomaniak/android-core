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
package com.infomaniak.core.common

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@NotTestedNorUsed
internal class DynamicLazyMapCacheManager<K, E>(
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
