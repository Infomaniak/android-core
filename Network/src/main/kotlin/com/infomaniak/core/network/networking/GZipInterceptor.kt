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

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.RealResponseBody
import okio.GzipSource
import okio.buffer

class GZipInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain) = unzip(chain.proceed(chain.request()))

    private fun unzip(response: Response): Response = with(response) {
        return if (body != null && headers["content-encoding"] == "gzip") {
            with(body!!) {
                val unzippedBody = GzipSource(source())
                val strippedHeaders = headers.newBuilder().build()
                newBuilder().headers(strippedHeaders)
                    .body(RealResponseBody(contentType().toString(), contentLength(), unzippedBody.buffer()))
                    .build()
            }
        } else {
            response
        }
    }
}
