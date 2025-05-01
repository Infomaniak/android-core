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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn

fun <K, E> DynamicLazyMap.Companion.sharedFlow(
    waitForCacheExpiration: (suspend DynamicLazyMap<K, SharedFlow<E>>.(key: K, flow: SharedFlow<E>) -> Unit)? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    createFlow: CoroutineScope.(K) -> Flow<E>,
): DynamicLazyMap<K, SharedFlow<E>> = DynamicLazyMap<K, SharedFlow<E>>(
    waitForCacheExpiration = waitForCacheExpiration,
    coroutineScope = coroutineScope,
    createElement = { key -> createFlow(key).shareIn(this, SharingStarted.Lazily, replay = 1) }
)

fun <K, E> DynamicLazyMap<K, SharedFlow<E>>.flowForKey(key: K): Flow<E> = flow {
    useElement(key) { sharedFlow: SharedFlow<E> ->
        emitAll(sharedFlow)
    }
}
