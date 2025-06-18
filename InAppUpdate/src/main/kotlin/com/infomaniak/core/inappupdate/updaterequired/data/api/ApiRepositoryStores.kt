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
package com.infomaniak.core.inappupdate.updaterequired.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.infomaniak.core.inappupdate.updaterequired.data.models.AppVersion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Date
import com.google.gson.JsonElement as GsonElement

object ApiRepositoryStores {

    var gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, CustomDateTypeAdapter())
        .create()

    fun generateRequestBody(body: Any?): RequestBody {
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val toJson = when (body) {
            is JsonElement, is GsonElement -> body.toString()
            is String -> body
            else -> gson.toJson(body)
        }
        return toJson.toRequestBody(jsonMediaType)
    }

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    fun getAppVersion(appName: String): AppVersion? {
        val request = Builder()
            .url(ApiRoutesStores.appVersion(appName))
            .get()
            .build()

        val body = OkHttpClient().newCall(request).execute().body

        return runCatching { json.decodeFromString<AppVersion>(body.toString()) }.getOrNull()
    }
}

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

