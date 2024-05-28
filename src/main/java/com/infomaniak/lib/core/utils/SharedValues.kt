/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2024 Infomaniak Network SA
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
package com.infomaniak.lib.core.utils

import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface SharedValues {

    val sharedPreferences: SharedPreferences

    fun sharedValue(key: String, defaultValue: Boolean): ReadWriteProperty<Any, Boolean> = with(sharedPreferences) {
        return object : ReadWriteProperty<Any, Boolean> {
            override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = getBoolean(key, defaultValue)
            override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) = transaction { putBoolean(key, value) }
        }
    }

    fun sharedValue(key: String, defaultValue: Int): ReadWriteProperty<Any, Int> = with(sharedPreferences) {
        return object : ReadWriteProperty<Any, Int> {
            override fun getValue(thisRef: Any, property: KProperty<*>): Int = getInt(key, defaultValue)
            override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) = transaction { putInt(key, value) }
        }
    }

    fun sharedValue(key: String, defaultValue: Float): ReadWriteProperty<Any, Float> = with(sharedPreferences) {
        return object : ReadWriteProperty<Any, Float> {
            override fun getValue(thisRef: Any, property: KProperty<*>): Float = getFloat(key, defaultValue)
            override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) = transaction { putFloat(key, value) }
        }
    }

    fun sharedValue(key: String, defaultValue: Long): ReadWriteProperty<Any, Long> = with(sharedPreferences) {
        return object : ReadWriteProperty<Any, Long> {
            override fun getValue(thisRef: Any, property: KProperty<*>): Long = getLong(key, defaultValue)
            override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) = transaction { putLong(key, value) }
        }
    }

    fun sharedValue(key: String, defaultValue: String): ReadWriteProperty<Any, String> = with(sharedPreferences) {
        return object : ReadWriteProperty<Any, String> {
            override fun getValue(thisRef: Any, property: KProperty<*>): String = getString(key, defaultValue) ?: defaultValue
            override fun setValue(thisRef: Any, property: KProperty<*>, value: String) = transaction { putString(key, value) }
        }
    }

    fun sharedValueNullable(key: String, defaultValue: String?): ReadWriteProperty<Any, String?> = with(sharedPreferences) {
        return object : ReadWriteProperty<Any, String?> {
            override fun getValue(thisRef: Any, property: KProperty<*>): String? = getString(key, defaultValue)
            override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) = transaction { putString(key, value) }
        }
    }

    fun sharedValue(key: String, defaultValue: Set<String>): ReadWriteProperty<Any, Set<String>> = with(sharedPreferences) {
        return object : ReadWriteProperty<Any, Set<String>> {
            override fun getValue(thisRef: Any, property: KProperty<*>) = getStringSet(key, defaultValue) ?: defaultValue
            override fun setValue(thisRef: Any, property: KProperty<*>, value: Set<String>) {
                transaction { putStringSet(key, value) }
            }
        }
    }

    fun sharedValue(key: String, defaultValue: List<String>): ReadWriteProperty<Any, List<String>> = with(sharedPreferences) {
        return object : ReadWriteProperty<Any, List<String>> {
            override fun getValue(thisRef: Any, property: KProperty<*>): List<String> {
                return getString(key, null)?.let(sharedValueJson::decodeFromString) ?: defaultValue
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: List<String>) {
                transaction { putString(key, sharedValueJson.encodeToString(value)) }
            }
        }
    }

    companion object {
        val sharedValueJson = Json
    }
}

inline fun <reified E : Enum<E>> SharedValues.sharedValue(key: String, defaultValue: E): ReadWriteProperty<Any, E> {
    return object : ReadWriteProperty<Any, E> {
        override fun getValue(thisRef: Any, property: KProperty<*>): E {
            return Utils.enumValueOfOrNull<E>(sharedPreferences.getString(key, defaultValue.name)) ?: defaultValue
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: E) {
            sharedPreferences.transaction { putString(key, value.name) }
        }
    }
}

inline fun <reified T : @Serializable Any> SharedValues.sharedValue(key: String, defaultValue: T?): ReadWriteProperty<Any, T?> {
    return object : ReadWriteProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            val stringRepresentation = sharedPreferences.getString(key, null) ?: return defaultValue
            return SharedValues.sharedValueJson.decodeFromString<T>(stringRepresentation)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) = sharedPreferences.transaction {
            putString(key, SharedValues.sharedValueJson.encodeToString(value))
        }
    }
}
