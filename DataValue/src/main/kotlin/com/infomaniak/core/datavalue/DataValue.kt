/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.datavalue

import kotlinx.coroutines.flow.Flow

/**
 * A single value persisted in a [androidx.datastore.core.DataStore].
 *
 * Instances are created through the `dataValue(…)` factories declared in [DataValues] and are meant to be exposed as `val`s:
 *
 * ```kotlin
 * class SettingsDataValues(appContext: Context) : DataValues(appContext, name = "settings") {
 *     val isUserAGoat = dataValue("isUserAGoat", false)
 * }
 * ```
 */
interface DataValue<T> {

    /** Emits the current value, then every subsequent value. Consecutive duplicates are filtered out. */
    val flow: Flow<T>

    /** Persists [value], overwriting whatever was stored previously. */
    suspend fun setValue(value: T)

    /** Atomically reads the current value, applies [transform], then persists the result. */
    suspend fun update(transform: suspend (T) -> T)
}
