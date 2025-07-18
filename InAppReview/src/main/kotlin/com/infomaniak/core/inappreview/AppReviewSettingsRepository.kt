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
package com.infomaniak.core.inappreview

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = AppReviewSettingsRepository.DATA_STORE_NAME,
    // In case we have a CorruptionException, we want to clear all DataStore preferences
    corruptionHandler = ReplaceFileCorruptionHandler<Preferences> { emptyPreferences() },
)

@Suppress("UNCHECKED_CAST")
class AppReviewSettingsRepository(private val context: Context) {

    internal var appReviewThreshold = DEFAULT_APP_REVIEW_THRESHOLD
    internal var maxAppReviewThreshold = appReviewThreshold * 10

    fun <T> flowOf(key: Preferences.Key<T>) = context.dataStore.data.map { it[key] ?: (getInitialValue(key) as T) }

    suspend fun <T> getValue(key: Preferences.Key<T>) = runCatching {
        flowOf(key).first()
    }.getOrElse { exception ->
        SentryLog.e(TAG, "Error while trying to get value from DataStore for key : $key", exception)
        getInitialValue(key) as T
    }

    private fun <T> getInitialValue(key: Preferences.Key<T>) = when (key) {
        APP_REVIEW_THRESHOLD_KEY -> appReviewThreshold
        ALREADY_GAVE_REVIEW_KEY -> DEFAULT_ALREADY_GAVE_REVIEW
        else -> throw IllegalArgumentException("Unknown Preferences.Key")
    }

    suspend fun <T> setValue(key: Preferences.Key<T>, value: T) {
        runCatching {
            context.dataStore.edit { it[key] = value }
        }.onFailure { exception ->
            SentryLog.e(TAG, "Error while trying to set value into DataStore for key : $key", exception)
        }
    }

    suspend fun clear() = context.dataStore.edit(MutablePreferences::clear)

    suspend fun resetReviewSettings() {
        setValue(APP_REVIEW_THRESHOLD_KEY, maxAppReviewThreshold)
    }

    companion object {

        private const val TAG = "AppReviewSettingsRepository"

        val APP_REVIEW_THRESHOLD_KEY = intPreferencesKey("appReviewThresholdKey")
        val ALREADY_GAVE_REVIEW_KEY = booleanPreferencesKey("alreadyGaveReview")

        internal const val DATA_STORE_NAME = "AppReviewSettingsDataStore"

        private const val DEFAULT_ALREADY_GAVE_REVIEW = false

        private const val DEFAULT_APP_REVIEW_THRESHOLD = 50
    }
}
