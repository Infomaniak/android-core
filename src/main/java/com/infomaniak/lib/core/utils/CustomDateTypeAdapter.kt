/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
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
 * Adapter to convert API [Long] timestamp to/from [Date] when [com.google.gson.Gson] deserializes/serializes a model.
 */
class CustomDateTypeAdapter : TypeAdapter<Date>() {
    /**
     * Write [Date] field as a [Long] timestamp or null
     */
    override fun write(out: JsonWriter?, date: Date?) {
        date?.let {
            out?.value(it.time / 1_000L)
        } ?: out?.nullValue()
    }

    /**
     * Read [Long] timestamp field as a [Date] or null
     */
    override fun read(timestamp: JsonReader?): Date? {
        return timestamp?.let {
            if (it.peek() === JsonToken.NUMBER) {
                Date(it.nextLong() * 1_000L)
            } else {
                it.nextNull()
                null
            }
        }
    }
}
