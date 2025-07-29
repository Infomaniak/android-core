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
import kotlin.reflect.KProperty

class ResponseHeaders(headers: Headers) {
    private val headersBacking = HeadersBacking(headers)

    val lastModified: String? by headersBacking.header("last-modified")
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
