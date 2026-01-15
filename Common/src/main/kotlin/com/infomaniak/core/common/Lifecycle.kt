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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.infomaniak.core.common

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach

fun Lifecycle.isResumedFlow() = currentStateFlow.map {
    it.isAtLeast(Lifecycle.State.RESUMED)
}.distinctUntilChanged()

fun Lifecycle.isStartedFlow() = currentStateFlow.map {
    it.isAtLeast(Lifecycle.State.STARTED)
}.distinctUntilChanged()

fun <T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job = onEach(action).launchInOnLifecycle(lifecycleOwner, state)

fun <T> Flow<T>.launchInOnLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
): Job {
    return launchInOnLifecycle(lifecycleOwner.lifecycle, state)
}

fun <T> Flow<T>.launchInOnLifecycle(
    lifecycle: Lifecycle,
    state: Lifecycle.State = Lifecycle.State.STARTED,
): Job {
    return lifecycle.currentStateFlow.mapLatest {
        it.isAtLeast(state)
    }.distinctUntilChanged().mapLatest { shouldBeHot ->
        if (shouldBeHot) this@launchInOnLifecycle.collect()
    }.launchIn(lifecycle.coroutineScope)
}
