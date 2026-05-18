/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2026 Infomaniak Network SA
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
package com.infomaniak.core.network.networking

import android.os.Build
import com.infomaniak.core.network.NetworkConfiguration
import com.infomaniak.core.network.utils.Utils.getPreferredLocaleList
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.util.appendIfNameAbsent
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import java.net.URLEncoder

object HttpUtils {

    /**
     * Generates a [Headers] object with common HTTP headers.
     *
     * **Note:** This function does not handle authorization tokens; they are managed by
     * [com.infomaniak.core.auth.TokenInterceptor]. If that interceptor isn't used,
     * add another one for token handling.
     */
    @ManualAuthorizationRequired
    fun getHeaders(contentType: String? = "application/json; charset=UTF-8"): Headers = headerMap(contentType).toHeaders()

    val getUserAgent: String by lazy {
        with(NetworkConfiguration) {
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

    fun HeadersBuilder.applyDefaultHeaders(contentType: ContentType? = ContentType.Application.Json) {
        headerMap(contentType?.toString()).forEach {
            appendIfNameAbsent(it.key, it.value)
        }
    }

    private fun headerMap(contentType: String?): Map<String, String> = with(NetworkConfiguration) {
        buildMap {
            put("Accept-Language", getAcceptedLanguageHeaderValue())
            put("Accept-Encoding", "gzip")
            put("User-Agent", getUserAgent)
            if (HttpClientConfig.cacheDir == null) put("Cache-Control", "no-cache")
            contentType?.let {
                put("Accept-type", contentType)
                put("Content-type", contentType)
            }
            customHeaders?.forEach { customHeader ->
                computeIfAbsent(customHeader.key) {
                    URLEncoder.encode(customHeader.value, "UTF-8")
                }
            }
        }
    }
}
