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

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext

/**
 * Always return if a select clause from [builder] was selected,
 * while allowing cancellation, if it happened strictly before one
 * of these clauses could be selected.
 */
suspend inline fun <R> trySelectAtomically(
    crossinline onCancellation: suspend () -> R,
    crossinline builder: SelectBuilder<R>.() -> Unit
): R? = coroutineScope {
    // We connect `cancellationSignal` to the current job, so it can be used as
    // a secondary select clause below, even though cancellation is blocked
    // using `withContext(NonCancellable)`.
    // The atomic behavior of `select` allows us to get the desired behavior.
    val cancellationSignal = launch { awaitCancellation() }
    withContext(NonCancellable) {
        select {
            builder() // We need to be biased towards this/these clause(s), so it comes first.
            cancellationSignal.onJoin { onCancellation() }
        }.also {
            // If a builder clause was selected, stop this job to allow coroutineScope to complete.
            cancellationSignal.cancel()
        }
    }
}
