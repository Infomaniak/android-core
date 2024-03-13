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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

//TODO: Inject this when kDrive will have hilt
private val Context.dataStore by preferencesDataStore(name = StoresSettingsRepository.DATA_STORE_NAME)

class StoresSettingsRepository(private val context: Context) {

    @Suppress("UNCHECKED_CAST")
    fun <T> flowOf(key: Preferences.Key<T>): Flow<T> {
        val defaultValue = when (key) {
            IS_USER_WANTING_UPDATES_KEY -> DEFAULT_IS_USER_WANTING_UPDATES
            HAS_APP_UPDATE_DOWNLOADED_KEY -> DEFAULT_HAS_APP_UPDATE_DOWNLOADED
            APP_UPDATE_LAUNCHES_KEY -> DEFAULT_APP_UPDATE_LAUNCHES
            APP_REVIEW_LAUNCHES_KEY -> DEFAULT_APP_REVIEW_LAUNCHES
            ALREADY_GAVE_REVIEW_KEY -> DEFAULT_ALREADY_GAVE_REVIEW
            else -> throw IllegalArgumentException("Unknown Preferences.Key")
        }

        return context.dataStore.data.map { it[key] ?: (defaultValue as T) }
    }

    suspend fun <T> getValue(key: Preferences.Key<T>): T = flowOf(key).first()

    suspend fun <T> setValue(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun clear() = context.dataStore.edit(MutablePreferences::clear)

    suspend fun resetUpdateSettings() {
        // This avoid the user being instantly reprompted to download update
        setValue(IS_USER_WANTING_UPDATES_KEY, DEFAULT_IS_USER_WANTING_UPDATES)
        setValue(HAS_APP_UPDATE_DOWNLOADED_KEY, DEFAULT_HAS_APP_UPDATE_DOWNLOADED)
        setValue(APP_UPDATE_LAUNCHES_KEY, DEFAULT_APP_UPDATE_LAUNCHES)
    }

    suspend fun resetReviewSettings() {
        setValue(APP_REVIEW_LAUNCHES_KEY, MAX_APP_REVIEW_LAUNCHES)
    }

    companion object {

        const val DEFAULT_APP_UPDATE_LAUNCHES = 20

        val IS_USER_WANTING_UPDATES_KEY = booleanPreferencesKey("isUserWantingUpdatesKey")
        val HAS_APP_UPDATE_DOWNLOADED_KEY = booleanPreferencesKey("hasAppUpdateDownloadedKey")
        val APP_UPDATE_LAUNCHES_KEY = intPreferencesKey("appUpdateLaunchesKey")
        val APP_REVIEW_LAUNCHES_KEY = intPreferencesKey("appReviewLaunchesKey")
        val ALREADY_GAVE_REVIEW_KEY = booleanPreferencesKey("alreadyGaveReview")

        internal const val DATA_STORE_NAME = "StoresSettingsDataStore"

        private const val DEFAULT_IS_USER_WANTING_UPDATES = false
        private const val DEFAULT_HAS_APP_UPDATE_DOWNLOADED = false
        private const val DEFAULT_ALREADY_GAVE_REVIEW = false

        private const val DEFAULT_APP_REVIEW_LAUNCHES = 50
        private const val MAX_APP_REVIEW_LAUNCHES = 500
    }
}
