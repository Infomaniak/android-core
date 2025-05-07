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
package com.infomaniak.core

import androidx.collection.mutableObjectListOf
import androidx.collection.mutableScatterMapOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import splitties.bitflags.hasFlag
import splitties.bitflags.withFlag
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Allows sharing and caching an element for a given key.
 *
 * It's similar to a read-only [Map], with these differences:
 * - Elements are lazily created with the passed [createElement] lambda.
 * - The use scope of an element is taken into account (with the block passed to [useElement] or [useElements]).
 * - An element is removed only after these 2 things happen without cancellation:
 *     1. It's no longer used
 *     2. The `waitForCacheExpiration` function of the passed [cacheManager] lambda completes.
 *
 * Note that [cacheManager] can access the [DynamicLazyMap] instance it's running on, giving access these [StateFlow] properties:
 * - [cachedElementsCount]
 * - [usedElementsCount]
 * - [totalElementsCount]
 *
 * This can be used to clean the cache dynamically, if it grows past a certain threshold, under certain circumstances.
 *
 * [DynamicLazyMap] Satisfies [this request de-duplication use-case](https://github.com/Kotlin/kotlinx.coroutines/issues/1097),
 * and other ones.
 */
@OptIn(DynamicLazyMap.Internals::class, ExperimentalContracts::class)
class DynamicLazyMap<K, E>(
    private val cacheManager: CacheManager<K, E>? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val createElement: CoroutineScope.(K) -> E,
) {

    companion object;

    fun interface CacheManager<K, E> {
        companion object;

        fun onUnused(
            currentCacheSize: Int,
            usedElementsCount: Int,
        ): OnUnusedBehavior = OnUnusedBehavior(cacheUntilExpired = true, evictOldest = false)

        suspend fun DynamicLazyMap<K, E>.waitForCacheExpiration(key: K, element: E)
    }

    @JvmInline
    value class OnUnusedBehavior internal constructor(internal val flags: UInt) {
        constructor(
            cacheUntilExpired: Boolean,
            evictOldest: Boolean,
        ) : this(
            0u.let {
                if (cacheUntilExpired) it.withFlag(Flags.cacheUntilExpired) else it
            }.let {
                if (evictOldest) it.withFlag(Flags.evictOldest) else it
            }
        )

        val cacheUntilExpired: Boolean get() = flags.hasFlag(Flags.cacheUntilExpired)
        val evictOldest: Boolean get() = flags.hasFlag(Flags.evictOldest)

        internal object Flags {
            //@formatter:off
            val cacheUntilExpired: UInt = 0b0000_0000_0000_0000_0000_0000_0000_0001u
            val evictOldest:       UInt = 0b0000_0000_0000_0000_0000_0000_0000_0010u
            //@formatter:on
        }
    }

    /** Get and use an element for the given [key]. */
    inline fun <R> useElement(
        key: K,
        block: (E) -> R
    ): R {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val element = getOrCreateElementWithRefCounting(key)
        return try {
            block(element)
        } finally {
            releaseRefForElement(key, element)
        }
    }

    /** Get and use all elements for the given [keys]. */
    inline fun <R> useElements(
        keys: Set<K>,
        block: (Map<K, E>) -> R
    ): R {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val elementMap = getOrCreateElementsWithRefCounting(keys)
        return try {
            block(elementMap)
        } finally {
            releaseRefForElements(elementMap)
        }
    }

    /**
     * The number of elements that are not used, but still in the cache.
     *
     * @see usedElementsCount
     * @see totalElementsCount
     */
    val cachedElementsCount: StateFlow<Int>

    /**
     * The number of elements actively used.
     *
     * @see cachedElementsCount
     * @see totalElementsCount
     */
    val usedElementsCount: StateFlow<Int>

    /**
     * The number of elements that are present (used, or cached).
     *
     * @see cachedElementsCount
     * @see usedElementsCount
     */
    val totalElementsCount: StateFlow<Int>

    @RequiresOptIn
    @Retention(AnnotationRetention.BINARY)
    private annotation class Internals

    private inner class Entry(
        val element: E,
        val subScope: CoroutineScope
    )

    private var _cachedElementsCount by MutableStateFlow(0).also { cachedElementsCount = it.asStateFlow() }::value
    private var _usedElementsCount by MutableStateFlow(0).also { usedElementsCount = it.asStateFlow() }::value
    private var _totalElementsCount by MutableStateFlow(0).also { totalElementsCount = it.asStateFlow() }::value

    private val refCounts = mutableScatterMapOf<K, Int>()
    private val elements = mutableScatterMapOf<K, Entry>()
    private val removers = mutableScatterMapOf<K, Job>()
    private val removersOrderedKeys = mutableObjectListOf<K>() // Because `removers` is a ScatterMap, which doesn't keep order.
    private val mapsEditLock: ReentrantLock = ReentrantLock()

    private fun updateCounts() {
        check(mapsEditLock.isHeldByCurrentThread)
        val total = elements.size
        val removersCount = removers.size
        _cachedElementsCount = removersCount
        _usedElementsCount = total - removersCount
        _totalElementsCount = total
    }

    @Internals
    @PublishedApi
    internal fun getOrCreateElementWithRefCounting(key: K): E = mapsEditLock.withLock {
        getOrCreateElementWithRefCountingUnchecked(key).also { updateCounts() }
    }

    @Internals
    @PublishedApi
    internal fun getOrCreateElementsWithRefCounting(keys: Set<K>): Map<K, E> = mapsEditLock.withLock {
        keys.associateWith { key ->
            getOrCreateElementWithRefCountingUnchecked(key)
        }.also { updateCounts() }
    }

    @Internals
    @PublishedApi
    internal fun releaseRefForElement(key: K, element: E) = mapsEditLock.withLock {
        releaseRefForElementUnchecked(key, element)
        updateCounts()
    }

    @Internals
    @PublishedApi
    internal fun releaseRefForElements(elementMap: Map<K, E>) = mapsEditLock.withLock {
        elementMap.forEach { (key, element) -> releaseRefForElementUnchecked(key, element) }
        updateCounts()
    }

    private fun getOrCreateElementWithRefCountingUnchecked(key: K): E {
        check(mapsEditLock.isHeldByCurrentThread)
        val currentCount = refCounts[key] ?: 0
        refCounts[key] = currentCount + 1
        val remover = removers[key]
        if (remover != null) {
            remover.cancel()
            removers.remove(key)
            removersOrderedKeys.remove(key)
        }
        return elements.getOrPut(key) {
            val newJob = Job(parent = coroutineScope.coroutineContext.job)
            val subScope = coroutineScope + newJob
            Entry(
                element = with(subScope) { createElement(key) },
                subScope = subScope
            )
        }.element.also { updateCounts() }
    }

    private fun releaseRefForElementUnchecked(key: K, element: E) {
        check(mapsEditLock.isHeldByCurrentThread)
        val newCount = refCounts[key]!! - 1
        when (newCount) {
            0 -> releaseOrCacheUnusedElement(key, element)
            else -> refCounts[key] = newCount
        }
    }

    private fun releaseOrCacheUnusedElement(key: K, element: E) {
        check(mapsEditLock.isHeldByCurrentThread)
        refCounts.remove(key)
        if (cacheManager == null) {
            elements.remove(key)?.subScope?.cancel()
            return
        }
        val behavior = cacheManager.onUnused(
            currentCacheSize = removers.size,
            usedElementsCount = elements.size - removers.size
        )
        if (behavior.evictOldest) {
            if (removersOrderedKeys.isEmpty()) {
                elements.remove(key)?.subScope?.cancel()
                return
            }
            val keyOfOldestElement = removersOrderedKeys.first()
            removeFromCache(keyOfOldestElement)
        }
        if (behavior.cacheUntilExpired) {
            removersOrderedKeys.add(key)
            removers[key] = coroutineScope.launch {
                with(cacheManager) { waitForCacheExpiration(key, element) }
                mapsEditLock.withLock {
                    if (refCounts[key] == null) {
                        removeFromCache(
                            key = key,
                            cancelRemover = false // Because we're in said remover, which is about to complete.
                        )
                        updateCounts()
                    }
                }
            }
        } else {
            elements.remove(key)?.subScope?.cancel()
        }
    }

    private fun removeFromCache(key: K, cancelRemover: Boolean = true) {
        check(mapsEditLock.isHeldByCurrentThread)
        removers.remove(key)?.also {
            if (cancelRemover) it.cancel()
        }
        removersOrderedKeys.remove(key)
        elements.remove(key)?.subScope?.cancel()
    }
}
