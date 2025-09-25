/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2025 Infomaniak Network SA
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
package com.infomaniak.core.auth.networking

import com.infomaniak.core.auth.TokenAuthenticator
import com.infomaniak.core.auth.TokenInterceptor
import com.infomaniak.core.auth.TokenInterceptorListener
import com.infomaniak.core.network.networking.HttpClient.addCache
import com.infomaniak.core.network.networking.HttpClient.addCommonInterceptors
import com.infomaniak.core.network.networking.HttpClient.addCustomTimeout
import okhttp3.OkHttpClient

object HttpClient : BaseHttpClient()

abstract class BaseHttpClient {

    protected var tokenInterceptorListener: TokenInterceptorListener? = null

    fun init(tokenInterceptorListener: TokenInterceptorListener) {
        this.tokenInterceptorListener = tokenInterceptorListener
    }

    val okHttpClientWithTokenInterceptor: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addCache()
            addTokenInterceptor()
            addCommonInterceptors()
        }.build()
    }

    val okHttpClientLongTimeoutWithTokenInterceptor: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                addCache()
                addTokenInterceptor()
                addCommonInterceptors()
                addCustomTimeout()
            }.build()
    }

    protected open fun OkHttpClient.Builder.addTokenInterceptor() {
        tokenInterceptorListener?.let { listener ->
            addInterceptor(TokenInterceptor(listener))
            authenticator(TokenAuthenticator(listener))
        }
    }
}
