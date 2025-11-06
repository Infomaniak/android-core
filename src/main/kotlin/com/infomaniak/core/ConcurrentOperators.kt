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

import kotlinx.coroutines.launch

suspend fun <T> Iterable<T>.allConcurrent(predicate: suspend (T) -> Boolean): Boolean {
    return anyMatchConcurrent(value = false, predicate = predicate)
}

suspend fun <T> Iterable<T>.anyConcurrent(predicate: suspend (T) -> Boolean): Boolean {
    return anyMatchConcurrent(value = true, predicate = predicate)
}

private suspend fun <T> Iterable<T>.anyMatchConcurrent(
    value: Boolean,
    predicate: suspend (T) -> Boolean,
): Boolean {
    if (this is Collection && isEmpty()) return value.not()
    return completableScope { completable ->
        for (element in this@anyMatchConcurrent) launch {
            if (predicate(element) == value) completable.complete(value)
        }
        value.not()
    }
}
