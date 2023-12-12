/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.networking

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.infomaniak.lib.core.BuildConfig
import com.infomaniak.lib.core.auth.TokenAuthenticator
import com.infomaniak.lib.core.auth.TokenInterceptor
import com.infomaniak.lib.core.auth.TokenInterceptorListener
import io.sentry.okhttp.SentryOkHttpInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClient {

    private lateinit var tokenInterceptorListener: TokenInterceptorListener

    var customInterceptor: List<Interceptor>? = null
    var customTimeout: Long = 2

    fun init(tokenInterceptorListener: TokenInterceptorListener) {
        this.tokenInterceptorListener = tokenInterceptorListener
    }

    val okHttpClientNoTokenInterceptor: OkHttpClient by lazy { OkHttpClient.Builder().apply { addInterceptors() }.build() }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptors()
            addTokenInterceptor()
        }.build()
    }

    val okHttpClientLongTimeoutNoTokenInterceptor: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                addInterceptors()
                addCustomTimeout()
            }.build()
    }

    val okHttpClientLongTimeout: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                addInterceptors()
                addTokenInterceptor()
                addCustomTimeout()
            }.build()
    }

    private fun OkHttpClient.Builder.addInterceptors() {
        if (BuildConfig.DEBUG) addNetworkInterceptor(StethoInterceptor())
        addInterceptor(SentryOkHttpInterceptor(captureFailedRequests = true))
        customInterceptor?.forEach { addInterceptor(it) }
    }

    private fun OkHttpClient.Builder.addTokenInterceptor() {
        addInterceptor(TokenInterceptor(tokenInterceptorListener))
        authenticator(TokenAuthenticator(tokenInterceptorListener))
    }

    private fun OkHttpClient.Builder.addCustomTimeout() {
        readTimeout(customTimeout, TimeUnit.MINUTES)
        writeTimeout(customTimeout, TimeUnit.MINUTES)
        connectTimeout(customTimeout, TimeUnit.MINUTES)
        callTimeout(customTimeout, TimeUnit.MINUTES)
    }
}
