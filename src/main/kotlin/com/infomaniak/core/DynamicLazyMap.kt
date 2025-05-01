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

import androidx.collection.mutableScatterMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
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
 * - An element is removed only after it's no longer used, and after the [waitForCacheExpiration] lambda completes.
 * Satisfies [this use-case](https://github.com/Kotlin/kotlinx.coroutines/issues/1097)
 */
@OptIn(DynamicLazyMap.Internals::class, ExperimentalContracts::class)
class DynamicLazyMap<K, E>(
    private val waitForCacheExpiration: (suspend (key: K, element: E) -> Unit)? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val createElement: CoroutineScope.(K) -> E,
) {

    companion object;

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

    @RequiresOptIn
    @Retention(AnnotationRetention.BINARY)
    private annotation class Internals

    private inner class Entry(
        val element: E,
        val subScope: CoroutineScope
    )

    private val refCounts = mutableScatterMapOf<K, Int>()
    private val elements = mutableScatterMapOf<K, Entry>()
    private val removers = mutableScatterMapOf<K, Job>()
    private val mapsEditLock: ReentrantLock = ReentrantLock()

    @Internals
    @PublishedApi
    internal fun getOrCreateElementWithRefCounting(key: K): E = mapsEditLock.withLock {
        val currentCount = refCounts[key] ?: 0
        refCounts[key] = currentCount + 1
        val remover = removers[key]
        if (remover != null) {
            remover.cancel()
            removers.remove(key)
        }
        elements.getOrPut(key) {
            val newJob = Job(parent = coroutineScope.coroutineContext.job)
            val subScope = coroutineScope + newJob
            Entry(
                element = with(subScope) { createElement(key) },
                subScope = subScope
            )
        }.element
    }

    @Internals
    @PublishedApi
    internal fun getOrCreateElementsWithRefCounting(keys: Set<K>): Map<K, E> = mapsEditLock.withLock {
        keys.associateWith { key ->
            getOrCreateElementWithRefCounting(key)
        }
    }

    @Internals
    @PublishedApi
    internal fun releaseRefForElement(key: K, element: E) = mapsEditLock.withLock {
        val newCount = refCounts[key]!! - 1
        if (newCount == 0) {
            refCounts.remove(key)
            if (waitForCacheExpiration == null) elements.remove(key)?.subScope?.cancel()
            else removers[key] = coroutineScope.launch {
                waitForCacheExpiration(key, element)
                mapsEditLock.withLock {
                    if (refCounts[key] == null) {
                        removers.remove(key)
                        elements.remove(key)?.subScope?.cancel()
                    }
                }
            }
        } else {
            refCounts[key] = newCount
        }
    }

    @Internals
    @PublishedApi
    internal fun releaseRefForElements(elementMap: Map<K, E>) = mapsEditLock.withLock {
        elementMap.forEach { (key, element) -> releaseRefForElement(key, element) }
    }
}
