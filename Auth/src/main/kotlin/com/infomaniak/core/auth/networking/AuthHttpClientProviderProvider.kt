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

import androidx.annotation.CallSuper
import com.infomaniak.core.auth.TokenAuthenticator
import com.infomaniak.core.auth.TokenInterceptor
import com.infomaniak.core.auth.TokenInterceptorListener
import com.infomaniak.core.network.networking.DefaultHttpClientProvider.addCache
import com.infomaniak.core.network.networking.DefaultHttpClientProvider.addCommonInterceptors
import com.infomaniak.core.network.networking.DefaultHttpClientProvider.addCustomTimeout
import okhttp3.OkHttpClient

object AuthHttpClientProvider : BaseHttpClientProvider()

abstract class BaseHttpClientProvider {

    protected open var tokenInterceptorListener: TokenInterceptorListener? = null

    val authOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addCache()
            addTokenInterceptor(builder = this, tokenInterceptorListener)
            addCommonInterceptors()
        }.build()
    }

    val authOkHttpClientLongTimeout: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                addCache()
                addTokenInterceptor(builder = this, tokenInterceptorListener)
                addCommonInterceptors()
                addCustomTimeout()
            }.build()
    }

    @CallSuper
    protected open fun addTokenInterceptor(builder: OkHttpClient.Builder, tokenInterceptorListener: TokenInterceptorListener?) {
        tokenInterceptorListener?.let { listener ->
            builder.addInterceptor(TokenInterceptor(listener))
            builder.authenticator(TokenAuthenticator(listener))
        }
    }

    internal fun setTokenInterceptorListener(tokenInterceptorListener: TokenInterceptorListener?) {
        this.tokenInterceptorListener = tokenInterceptorListener
    }
}
