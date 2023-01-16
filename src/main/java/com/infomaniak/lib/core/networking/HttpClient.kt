/*
 * Infomaniak Core - Android
 * Copyright (C) 2023 Infomaniak Network SA
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
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClient {

    private lateinit var tokenInterceptorListener: TokenInterceptorListener

    fun init(tokenInterceptorListener: TokenInterceptorListener) {
        this.tokenInterceptorListener = tokenInterceptorListener
    }

    val okHttpClientNoInterceptor: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) addNetworkInterceptor(StethoInterceptor())
            }.build()
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) addNetworkInterceptor(StethoInterceptor())
                addInterceptor(TokenInterceptor(tokenInterceptorListener))
                authenticator(TokenAuthenticator(tokenInterceptorListener))
            }.build()
    }

    val okHttpClientLongTimeoutNoInterceptor: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) addNetworkInterceptor(StethoInterceptor())
                readTimeout(2, TimeUnit.MINUTES)
                writeTimeout(2, TimeUnit.MINUTES)
                connectTimeout(2, TimeUnit.MINUTES)
                callTimeout(2, TimeUnit.MINUTES)
            }.build()
    }

    val okHttpClientLongTimeout: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) addNetworkInterceptor(StethoInterceptor())
                addInterceptor(TokenInterceptor(tokenInterceptorListener))
                authenticator(TokenAuthenticator(tokenInterceptorListener))
                readTimeout(2, TimeUnit.MINUTES)
                writeTimeout(2, TimeUnit.MINUTES)
                connectTimeout(2, TimeUnit.MINUTES)
                callTimeout(2, TimeUnit.MINUTES)
            }.build()
    }
}
