/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2024 Infomaniak Network SA
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
package com.infomaniak.lib.core.utils

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.infomaniak.lib.core.auth.TokenAuthenticator
import com.infomaniak.lib.core.auth.TokenInterceptor
import com.infomaniak.lib.core.auth.TokenInterceptorListener
import com.infomaniak.lib.core.networking.HttpClientConfig
import com.infomaniak.lib.core.networking.HttpUtils
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object CoilUtils {

    private const val COIL_CACHE_DIR = "coil_cache"

    private var simpleImageLoader: ImageLoader? = null

    fun simpleImageLoader(context: Context): ImageLoader {
        return simpleImageLoader ?: synchronized(this) {
            simpleImageLoader?.let { return it }
            newImageLoader(context).also {
                simpleImageLoader = it
            }
        }
    }

    inline val Context.simpleImageLoader: ImageLoader get() = simpleImageLoader(this)

    fun newImageLoader(
        context: Context,
        tokenInterceptorListener: TokenInterceptorListener? = null,
        gifPreview: Boolean = false,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                if (gifPreview) {
                    val factory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoderDecoder.Factory()
                    } else {
                        GifDecoder.Factory()
                    }
                    add(factory)
                }
            }
            .okHttpClient {
                OkHttpClient.Builder().apply {

                    HttpClientConfig.apply { cacheDir?.let { cache(Cache(it, CACHE_SIZE_BYTES)) } }

                    tokenInterceptorListener?.let {
                        addInterceptor(Interceptor { chain ->
                            chain.request().newBuilder()
                                .headers(HttpUtils.getHeaders())
                                .build()
                                .let(chain::proceed)
                        })

                        addInterceptor(TokenInterceptor(it))
                        authenticator(TokenAuthenticator(it))

                        // Add common interceptors after every other interceptor so everything is already setup correctly by other
                        // previous interceptors. Especially needed by the custom interceptors like AccessTokenUsageInterceptor
                        HttpClientConfig.addCommonInterceptors(this)
                    }
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
