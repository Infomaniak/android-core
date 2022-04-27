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
package com.infomaniak.lib.core.networking

import androidx.core.os.LocaleListCompat
import com.infomaniak.lib.core.InfomaniakCore
import okhttp3.Headers
import java.net.URLEncoder
import java.util.*

object HttpUtils {

    fun getHeaders(contentType: String? = "application/json; charset=UTF-8"): Headers {
        return Headers.Builder().apply {
            add("Accept-Language", getAcceptedLanguageHeaderValue())
            add("App-Version", "Android ${InfomaniakCore.appVersionName}")
            add("Authorization", "Bearer ${InfomaniakCore.bearerToken}")
            add("Cache-Control", "no-cache")
            contentType?.let { add("Content-type", it) }
            InfomaniakCore.deviceIdentifier?.let { add("Device-Identifier", URLEncoder.encode(it, "UTF-8")) }
        }.run {
            build()
        }
    }

    private fun getAcceptedLanguageHeaderValue(): String {
        var weight = 1.0F
        return getPreferredLocaleList()
            .map { it.toLanguageTag() }
            .reduce { accumulator, languageTag ->
                weight -= 0.1F
                "$accumulator,$languageTag;q=$weight"
            }
    }

    private fun getPreferredLocaleList(): List<Locale> {
        val adjustedLocaleListCompat = LocaleListCompat.getAdjustedDefault()
        val preferredLocaleList = mutableListOf<Locale>()
        for (index in 0 until adjustedLocaleListCompat.size()) {
            preferredLocaleList.add(adjustedLocaleListCompat.get(index))
        }
        return preferredLocaleList
    }
}
