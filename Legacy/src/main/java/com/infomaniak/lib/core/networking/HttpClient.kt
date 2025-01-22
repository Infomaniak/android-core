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

import com.infomaniak.lib.core.auth.TokenAuthenticator
import com.infomaniak.lib.core.auth.TokenInterceptor
import com.infomaniak.lib.core.auth.TokenInterceptorListener
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClient {

    private lateinit var tokenInterceptorListener: TokenInterceptorListener

    fun init(tokenInterceptorListener: TokenInterceptorListener) {
        this.tokenInterceptorListener = tokenInterceptorListener
    }

    val okHttpClientNoTokenInterceptor: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addCache()
            addCommonInterceptors()
        }.build()
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addCache()
            addTokenInterceptor()
            addCommonInterceptors()
        }.build()
    }

    val okHttpClientLongTimeoutNoTokenInterceptor: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                addCache()
                addCommonInterceptors()
                addCustomTimeout()
            }.build()
    }

    val okHttpClientLongTimeout: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                addCache()
                addTokenInterceptor()
                addCommonInterceptors()
                addCustomTimeout()
            }.build()
    }

    private fun OkHttpClient.Builder.addCache() {
        HttpClientConfig.apply { cacheDir?.let { cache(Cache(it, CACHE_SIZE_BYTES)) } }
    }

    /**
     * Add common interceptors after every other interceptor so everything is already setup correctly by other previous
     * interceptors. Especially needed by the custom interceptors like AccessTokenUsageInterceptor
     * */
    private fun OkHttpClient.Builder.addCommonInterceptors() {
        HttpClientConfig.addCommonInterceptors(this)
    }

    private fun OkHttpClient.Builder.addTokenInterceptor() {
        addInterceptor(TokenInterceptor(tokenInterceptorListener))
        authenticator(TokenAuthenticator(tokenInterceptorListener))
    }

    private fun OkHttpClient.Builder.addCustomTimeout() {
        HttpClientConfig.apply {
            readTimeout(customTimeoutMinutes, TimeUnit.MINUTES)
            writeTimeout(customTimeoutMinutes, TimeUnit.MINUTES)
            connectTimeout(customTimeoutMinutes, TimeUnit.MINUTES)
            callTimeout(customTimeoutMinutes, TimeUnit.MINUTES)
        }
    }
}
