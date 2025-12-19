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
package com.infomaniak.core.inappupdate

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.infomaniak.core.sentry.SentryLog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = AppUpdateSettingsRepository.DATA_STORE_NAME,
    // In case we have a CorruptionException, we want to clear all DataStore preferences
    corruptionHandler = ReplaceFileCorruptionHandler<Preferences> { emptyPreferences() },
)

@Suppress("UNCHECKED_CAST")
class AppUpdateSettingsRepository(private val context: Context) {

    fun <T> flowFor(key: Preferences.Key<T>) = context.dataStore.data
        .map { it[key] ?: (getDefaultValue(key) as T) }
        .distinctUntilChanged()

    suspend fun <T> getValue(key: Preferences.Key<T>) = runCatching {
        flowFor(key).first()
    }.getOrElse { exception ->
        SentryLog.e(TAG, "Error while trying to get value from DataStore for key : $key", exception)
        getDefaultValue(key) as T
    }

    private fun <T> getDefaultValue(key: Preferences.Key<T>) = when (key) {
        IS_USER_WANTING_UPDATES_KEY -> DEFAULT_IS_USER_WANTING_UPDATES
        HAS_APP_UPDATE_DOWNLOADED_KEY -> DEFAULT_HAS_APP_UPDATE_DOWNLOADED
        APP_UPDATE_LAUNCHES_KEY -> DEFAULT_APP_UPDATE_LAUNCHES
        else -> throw IllegalArgumentException("Unknown Preferences.Key")
    }

    suspend fun <T> setValue(key: Preferences.Key<T>, value: T) {
        runCatching {
            context.dataStore.edit { it[key] = value }
        }.onFailure { exception ->
            SentryLog.e(TAG, "Error while trying to set value into DataStore for key : $key", exception)
        }
    }

    suspend fun clear() {
        context.dataStore.edit(MutablePreferences::clear)
    }

    suspend fun resetUpdateSettings() {
        // This avoid the user being instantly reprompted to download update
        setValue(IS_USER_WANTING_UPDATES_KEY, DEFAULT_IS_USER_WANTING_UPDATES)
        setValue(HAS_APP_UPDATE_DOWNLOADED_KEY, DEFAULT_HAS_APP_UPDATE_DOWNLOADED)
        setValue(APP_UPDATE_LAUNCHES_KEY, DEFAULT_APP_UPDATE_LAUNCHES)
    }

    companion object {

        private const val TAG = "AppUpdateSettingsRepository"

        const val DEFAULT_APP_UPDATE_LAUNCHES = 20

        val IS_USER_WANTING_UPDATES_KEY = booleanPreferencesKey("isUserWantingUpdatesKey")
        val HAS_APP_UPDATE_DOWNLOADED_KEY = booleanPreferencesKey("hasAppUpdateDownloadedKey")
        val APP_UPDATE_LAUNCHES_KEY = intPreferencesKey("appUpdateLaunchesKey")

        internal const val DATA_STORE_NAME = "AppUpdateSettingsDataStore"

        private const val DEFAULT_IS_USER_WANTING_UPDATES = false
        private const val DEFAULT_HAS_APP_UPDATE_DOWNLOADED = false
    }
}
