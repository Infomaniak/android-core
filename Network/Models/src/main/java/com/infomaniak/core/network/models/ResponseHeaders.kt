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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.infomaniak.core.network.models

import okhttp3.Headers
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KProperty

class ResponseHeaders(headers: Headers) {
    private val headersBacking = HeadersBacking(headers)

    private val _lastModified: String? by headersBacking.header("last-modified")
    // TODO: Use [Instant] from kotlin when Kotlin version is bump to 2.1.0
    val lastModified: Instant by lazy { ZonedDateTime.parse(_lastModified, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant() }
}

private class HeadersBacking(val remoteHeaders: Headers) {
    fun header(key: String): HeaderData {
        return object : HeaderData {
            override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
                return remoteHeaders[key]
            }
        }
    }
}

private interface HeaderData {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String?
}
