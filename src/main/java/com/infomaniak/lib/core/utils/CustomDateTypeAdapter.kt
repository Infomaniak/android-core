/*
 * Infomaniak Core - Android
 * Copyright (C) 2021 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.util.*

/**
 * Adapter for convert api timestamp to java [Date] when [com.google.gson.Gson] deserialize a model
 * And convert method [Date] to [Long] timestamp for the api when [com.google.gson.Gson] serialize a model
 */
class CustomDateTypeAdapter : TypeAdapter<Date>() {
    /**
     * Write fields with type [Date] as a [Long] timestamp or null field
     */
    override fun write(out: JsonWriter?, date: Date?) {
        date?.let {
            out?.value(date.time / 1000)
        } ?: out?.nullValue()
    }

    /**
     * Read fields timestamp [Long] as [Date]
     * Read json fields [Long] timestamp as a [Date]
     */
    override fun read(timestamp: JsonReader?): Date? {
        return timestamp?.let {
            if (timestamp.peek() === JsonToken.NULL) {
                timestamp.nextNull()
                null
            } else {
                Date(it.nextLong() * 1000)
            }
        }
    }
}
