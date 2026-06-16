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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class PreferenceDataValue<T, R : Any>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<R>,
    private val defaultValue: T,
    private val encode: (T) -> R?,
    private val decode: (R) -> T,
) : DataValue<T> {

    override val flow: Flow<T> = dataStore.data.map { it.read() }.distinctUntilChanged()

    override suspend fun setValue(value: T) {
        dataStore.edit { it.write(value) }
    }

    override suspend fun update(transform: suspend (T) -> T) {
        dataStore.edit { it.write(transform(it.read())) }
    }

    private fun Preferences.read(): T = this[key]?.let(decode) ?: defaultValue

    private fun MutablePreferences.write(value: T) {
        val encoded = encode(value)
        if (encoded == null) remove(key) else set(key, encoded)
    }
}
