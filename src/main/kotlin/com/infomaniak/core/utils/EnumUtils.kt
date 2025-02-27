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
package com.infomaniak.core.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

inline fun <reified T : Enum<T>> enumValueOfOrNull(value: String?): T? {
    return value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() }
}

inline fun <reified T> apiEnumValueOfOrNull(value: String?): T? where T : Enum<T>, T : ApiEnum {
    return value?.let { runCatching { enumValues<T>().firstOrNull { it.apiValue == value } }.getOrNull() }
}

inline fun <reified T> apiEnum(backingFieldProperty: KMutableProperty0<String?>): ReadWriteProperty<Any?, T?> where T : Enum<T>, T : ApiEnum {
    return object : ReadWriteProperty<Any?, T?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T? = apiEnumValueOfOrNull<T>(backingFieldProperty.get())
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = backingFieldProperty.set(value?.apiValue)
    }
}

interface ApiEnum {
    val apiValue: String
}
