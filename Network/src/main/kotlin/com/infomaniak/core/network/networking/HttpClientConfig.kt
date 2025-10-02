/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2025 Infomaniak Network SA
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

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.infomaniak.core.network.BuildConfig
import io.sentry.okhttp.SentryOkHttpInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File

object HttpClientConfig {

    const val CACHE_SIZE_BYTES: Long = 10L * 1_024L * 1_024L // 10 MB

    var cacheDir: File? = null
    var customInterceptors: List<Interceptor>? = null
    var customTimeoutMinutes = 2L

    /**
     * Add common interceptors after every other interceptor so everything is already setup correctly by other previous
     * interceptors. Especially needed by the custom interceptors like AccessTokenUsageInterceptor
     * */
    fun addCommonInterceptors(builder: OkHttpClient.Builder) = with(builder) {
        if (BuildConfig.DEBUG) addNetworkInterceptor(StethoInterceptor())
        addInterceptor(GZipInterceptor())
        addInterceptor(SentryOkHttpInterceptor(captureFailedRequests = true))
        addInterceptor(UrlTraceInterceptor())

        customInterceptors?.forEach(::addInterceptor)
    }
}
