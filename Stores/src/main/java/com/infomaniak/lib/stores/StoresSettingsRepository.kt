/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.lib.stores

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

//TODO: Inject this when kDrive will have hilt
private val Context.dataStore by preferencesDataStore(name = StoresSettingsRepository.DATA_STORE_NAME)

class StoresSettingsRepository(private val context: Context) {

    val flow: Flow<StoresSettings> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it.toStoresSettings() }

    @Suppress("UNCHECKED_CAST")
    fun <T> flowOf(key: Preferences.Key<T>): Flow<T> {
        val defaultValue = when (key) {
            IS_USER_WANTING_UPDATES -> DEFAULT_IS_USER_WANTING_UPDATES
            HAS_APP_UPDATE_DOWNLOADED -> DEFAULT_HAS_APP_UPDATE_DOWNLOADED
            APP_UPDATE_LAUNCHES -> DEFAULT_APP_UPDATE_LAUNCHES
            else -> throw IllegalArgumentException("Unknown Preferences.Key")
        }

        return context.dataStore.data.map { it[key] ?: (defaultValue as T) }
    }

    suspend fun getAll() = context.dataStore.data.first().toStoresSettings()

    suspend fun <T> getValue(key: Preferences.Key<T>): T = flowOf(key).first()

    suspend fun <T> setValue(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun clear() = context.dataStore.edit(MutablePreferences::clear)

    suspend fun resetUpdateSettings() {
        // This avoid the user being instantly reprompted to download update
        setValue(IS_USER_WANTING_UPDATES, false)
        setValue(HAS_APP_UPDATE_DOWNLOADED, false)
    }

    private fun Preferences.toStoresSettings() = StoresSettings(
        isUserWantingUpdates = this[IS_USER_WANTING_UPDATES] ?: DEFAULT_IS_USER_WANTING_UPDATES,
        hasAppUpdateDownloaded = this[HAS_APP_UPDATE_DOWNLOADED] ?: DEFAULT_HAS_APP_UPDATE_DOWNLOADED,
        appUpdateLaunches = this[APP_UPDATE_LAUNCHES] ?: DEFAULT_APP_UPDATE_LAUNCHES,
    )

    data class StoresSettings(
        val isUserWantingUpdates: Boolean,
        val hasAppUpdateDownloaded: Boolean,
        val appUpdateLaunches: Int,
    )

    companion object {

        val IS_USER_WANTING_UPDATES = booleanPreferencesKey("isUserWantingUpdatesKey")
        val HAS_APP_UPDATE_DOWNLOADED = booleanPreferencesKey("hasAppUpdateDownloadedKey")
        val APP_UPDATE_LAUNCHES = intPreferencesKey("appUpdateLaunchesKey")

        const val DATA_STORE_NAME = "StoresSettingsDataStore"

        private const val DEFAULT_IS_USER_WANTING_UPDATES = true
        private const val DEFAULT_HAS_APP_UPDATE_DOWNLOADED = false
        const val DEFAULT_APP_UPDATE_LAUNCHES = 20
    }
}
