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
package com.infomaniak.core.coil

import android.content.Context
import coil3.ImageLoader
import coil3.decode.Decoder
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.infomaniak.core.auth.TokenAuthenticator
import com.infomaniak.core.auth.TokenInterceptor
import com.infomaniak.core.auth.TokenInterceptorListener
import com.infomaniak.core.network.networking.HttpClientConfig
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.network.networking.ManualAuthorizationRequired
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object ImageLoaderProvider {

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
        customFactories: List<Decoder.Factory> = emptyList()
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                customFactories.forEach(::add)
                add(OkHttpNetworkFetcherFactory(callFactory = { coilHttpClient(tokenInterceptorListener) }))
            }
            .memoryCache {
                MemoryCache.Builder().maxSizePercent(context).build()
            }
            .diskCache {
                DiskCache.Builder().directory(context.cacheDir.resolve(COIL_CACHE_DIR)).build()
            }
            .build()
    }

    private fun coilHttpClient(tokenInterceptorListener: TokenInterceptorListener?): OkHttpClient =
        OkHttpClient.Builder().apply {
            HttpClientConfig.apply { cacheDir?.let { cache(Cache(it, CACHE_SIZE_BYTES)) } }

            tokenInterceptorListener?.let {
                this.addInterceptor(Interceptor { chain ->
                    @OptIn(ManualAuthorizationRequired::class)
                    chain.request().newBuilder()
                        .headers(HttpUtils.getHeaders())
                        .build()
                        .let(chain::proceed)
                })

                this.addInterceptor(TokenInterceptor(it))
                this.authenticator(TokenAuthenticator(it))
            }

            HttpClientConfig.addCommonInterceptors(this) // Needs to be added last
        }.build()
}
