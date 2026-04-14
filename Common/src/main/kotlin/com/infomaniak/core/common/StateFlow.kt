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

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
fun <T, R> StateFlow<T>.mapSync(transform: (T) -> R): StateFlow<R> = object : StateFlow<R> {

    override val replayCache: List<R> get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        var lastEmittedValue: Any? = nullSurrogate
        this@mapSync.collect { newValue ->
            val transformedValue = transform(newValue)
            if (transformedValue != lastEmittedValue) {
                lastEmittedValue = transformedValue
                collector.emit(transformedValue)
            }
        }
    }

    private var lastUpstreamValue = this@mapSync.value

    override var value: R = transform(lastUpstreamValue)
        private set
        get() {
            val currentUpstreamValue: T = this@mapSync.value
            if (currentUpstreamValue == lastUpstreamValue) return field
            val newValue = transform(currentUpstreamValue)
            field = newValue
            lastUpstreamValue = currentUpstreamValue
            return newValue
        }
}

private val nullSurrogate = Any()

suspend fun <T> MutableStateFlow<T>.updateOnce(
    valueToWaitFor: T,
    newValue: T,
) {
    while (true) {
        currentCoroutineContext().ensureActive()
        if (valueToWaitFor == value && compareAndSet(valueToWaitFor, newValue)) return
        first { it == value }
    }
}

suspend fun <T> MutableStateFlow<T>.updateOnce(
    canUpdate: (currentValue: T) -> Boolean,
    newValue: T,
) {
    while (true) {
        currentCoroutineContext().ensureActive()
        val currentValue = value
        if (canUpdate(currentValue) && compareAndSet(currentValue, newValue)) return
        first(canUpdate)
    }
}
