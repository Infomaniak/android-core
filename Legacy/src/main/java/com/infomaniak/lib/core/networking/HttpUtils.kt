/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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
package com.infomaniak.lib.core.networking

import android.os.Build
import com.infomaniak.lib.core.InfomaniakCore
import com.infomaniak.lib.core.utils.Utils.getPreferredLocaleList
import okhttp3.Headers

object HttpUtils {

    /**
     * Generates a [Headers] object with common HTTP headers.
     *
     * **Note:** This function does not handle authorization tokens; they are managed by
     * [com.infomaniak.lib.core.auth.TokenInterceptor]. If that interceptor isn't used,
     * add another one for token handling.
     */
    @ManualAuthorizationRequired
    fun getHeaders(contentType: String? = "application/json; charset=UTF-8"): Headers = with(InfomaniakCore) {
        return Headers.Builder().apply {
            add("Accept-Language", getAcceptedLanguageHeaderValue())
            add("Accept-Encoding", "gzip")
            add("App-Version", "Android $appVersionName")
            add("User-Agent", getUserAgent)
            if (HttpClientConfig.cacheDir == null) add("Cache-Control", "no-cache")
            contentType?.let {
                add("Accept-type", it)
                add("Content-type", it)
            }
            customHeaders.forEach { customHeader -> add(customHeader.headerKey, customHeader.headerValue) }
        }.run {
            build()
        }
    }

    val getUserAgent: String by lazy {
        with(InfomaniakCore) {
            val androidVersion = "Android ${Build.VERSION.RELEASE}"
            val arch = System.getProperty("os.arch")
            "$appId/$appVersionName-$appVersionCode (${Build.MODEL}; $androidVersion; $arch)"
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
}
