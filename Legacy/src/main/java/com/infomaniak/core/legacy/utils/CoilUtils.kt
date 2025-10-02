/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2025 Infomaniak Network SA
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
package com.infomaniak.core.legacy.utils

import android.content.Context
import coil.ImageLoader
import coil.decode.Decoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.infomaniak.core.legacy.auth.TokenAuthenticator
import com.infomaniak.core.legacy.auth.TokenInterceptor
import com.infomaniak.core.legacy.auth.TokenInterceptorListener
import com.infomaniak.core.legacy.networking.HttpClientConfig
import com.infomaniak.core.legacy.networking.HttpUtils
import com.infomaniak.core.legacy.networking.ManualAuthorizationRequired
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient

@Deprecated("Use the object exposed through the Core:Coil module")
object CoilUtils {

    private const val COIL_CACHE_DIR = "coil_cache"

    private var simpleImageLoader: ImageLoader? = null

    @Deprecated("Use the method exposed through the Core:Coil module")
    fun simpleImageLoader(context: Context): ImageLoader {
        return simpleImageLoader ?: synchronized(this) {
            simpleImageLoader?.let { return it }
            @Suppress("DEPRECATION")
            newImageLoader(context).also {
                simpleImageLoader = it
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use the property exposed through the Core:Coil module")
    inline val Context.simpleImageLoader: ImageLoader get() = simpleImageLoader(this)

    @Deprecated("Use the method exposed through the Core:Coil module")
    fun newImageLoader(
        context: Context,
        tokenInterceptorListener: TokenInterceptorListener? = null,
        customFactories: List<Decoder.Factory> = emptyList()
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                customFactories.forEach(::add)
            }
            .okHttpClient {
                OkHttpClient.Builder().apply {

                    HttpClientConfig.apply { cacheDir?.let { cache(Cache(it, CACHE_SIZE_BYTES)) } }

                    tokenInterceptorListener?.let {
                        addInterceptor(Interceptor { chain ->
                            @OptIn(ManualAuthorizationRequired::class)
                            chain.request().newBuilder()
                                .headers(HttpUtils.getHeaders())
                                .build()
                                .let(chain::proceed)
                        })

                        addInterceptor(TokenInterceptor(it))
                        authenticator(TokenAuthenticator(it))
                    }

                    HttpClientConfig.addCommonInterceptors(this) // Needs to be added last
                }.build()
            }
            .memoryCache {
                MemoryCache.Builder(context).build()
            }
            .diskCache {
                DiskCache.Builder().directory(context.cacheDir.resolve(COIL_CACHE_DIR)).build()
            }
            .build()
    }
}
