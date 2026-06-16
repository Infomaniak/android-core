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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Easy to use wrapper to interact with the DataStore.
 *
 * Extend this class to have access to the `dataValue()` methods to define and access values stored in the DataStore.
 *
 * It supports storing primitive types, serializable types, and their respective nullable versions.
 */
abstract class DataValues(appContext: Context, name: String) {

    private val dataStore: DataStore<Preferences> = appContext.applicationContext.dataValueStore(name)

    // region Primitives (stored natively)
    protected fun dataValue(key: String, defaultValue: Boolean): DataValue<Boolean> =
        native(booleanPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Int): DataValue<Int> =
        native(intPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Long): DataValue<Long> =
        native(longPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Float): DataValue<Float> =
        native(floatPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Double): DataValue<Double> =
        native(doublePreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: String): DataValue<String> =
        native(stringPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Set<String>): DataValue<Set<String>> =
        native(stringSetPreferencesKey(key), defaultValue)
    // endregion

    // region Nullable primitives (stored natively, a null write clears the entry)
    protected fun dataValue(key: String, defaultValue: Boolean?): DataValue<Boolean?> =
        nullableNative(booleanPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Int?): DataValue<Int?> =
        nullableNative(intPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Long?): DataValue<Long?> =
        nullableNative(longPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Float?): DataValue<Float?> =
        nullableNative(floatPreferencesKey(key), defaultValue)

    protected fun dataValue(key: String, defaultValue: Double?): DataValue<Double?> =
        nullableNative(doublePreferencesKey(key), defaultValue)

    @JvmName("dataValueNullableString")
    protected fun dataValue(key: String, defaultValue: String?): DataValue<String?> =
        nullableNative(stringPreferencesKey(key), defaultValue)

    @JvmName("dataValueNullableStringSet")
    protected fun dataValue(key: String, defaultValue: Set<String>?): DataValue<Set<String>?> =
        nullableNative(stringSetPreferencesKey(key), defaultValue)
    // endregion

    // region Serializable / custom
    /**
     * Stores any [kotlinx.serialization.Serializable] [T] (this includes built-ins such as [Pair], [List], enums…).
     * Nullable types are supported and round-trip correctly (an explicit `null` is persisted as such).
     */
    protected inline fun <reified T> dataValue(key: String, defaultValue: T): DataValue<T> =
        serializableDataValue(key, defaultValue, serializer())

    /** Stores [T] using a user-provided [DataValueSerializer], for types that aren't natively supported. */
    protected fun <T> dataValue(key: String, defaultValue: T, serializer: DataValueSerializer<T>): DataValue<T> =
        build(
            key = stringPreferencesKey(key),
            defaultValue = defaultValue,
            encode = serializer::serialize,
            decode = serializer::deserialize,
        )
    // endregion

    @PublishedApi
    internal fun <T> serializableDataValue(key: String, defaultValue: T, serializer: KSerializer<T>): DataValue<T> =
        build(
            key = stringPreferencesKey(key),
            defaultValue = defaultValue,
            encode = { json.encodeToString(serializer, it) },
            decode = { json.decodeFromString(serializer, it) },
        )

    private fun <T : Any> native(key: Preferences.Key<T>, defaultValue: T): DataValue<T> =
        build(key, defaultValue, encode = { it }, decode = { it })

    private fun <T : Any> nullableNative(key: Preferences.Key<T>, defaultValue: T?): DataValue<T?> =
        build(key, defaultValue, encode = { it }, decode = { it })

    private fun <T, R : Any> build(
        key: Preferences.Key<R>,
        defaultValue: T,
        encode: (T) -> R?,
        decode: (R) -> T,
    ): DataValue<T> = PreferenceDataValue(dataStore, key, defaultValue, encode, decode)

    companion object {
        private val json by lazy { Json }

        private fun Context.dataValueStore(name: String): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                // In case of a CorruptionException, clear everything rather than crashing.
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                produceFile = { preferencesDataStoreFile(name) },
            )
    }
}
