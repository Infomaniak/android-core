/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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

@file:OptIn(ExperimentalContracts::class)

import com.infomaniak.core.network.api.ApiController
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.network.networking.ManualAuthorizationRequired
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun createHttpClient(
    connectedOkHttpClient: OkHttpClient,
    json: Json = ApiController.json,
): HttpClient = HttpClient(OkHttp) {
    engine { preconfigured = connectedOkHttpClient }
    install(ContentNegotiation) {
        json(json)
    }
    install(HttpRequestRetry) {
        maxRetries = 3
        // TODO[networking-improvements]: Interface with ConnectivityManager and don't retry if not currently connected.
        retryOnExceptionIf { _, cause -> cause !is SerializationException }
    }
    defaultRequest {
        userAgent(HttpUtils.getUserAgent)
        headers {
            @OptIn(ManualAuthorizationRequired::class) // Already handled by the http client.
            HttpUtils.getHeaders().forEach { (header, value) -> append(header, value) }
        }
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }
    HttpResponseValidator {
        validateResponse { response ->
            response.validateContentType { accepted, received ->
                val url = response.request.url
                val method = response.request.method
                throw IllegalArgumentException("Expected Content-Type $accepted but got $received from $method on $url")
            }
        }
    }
}

private inline fun HttpResponse.validateContentType(
    onContentTypeMismatch: (accepted: String, received: String?) -> Unit
) {
    contract { callsInPlace(onContentTypeMismatch, InvocationKind.AT_MOST_ONCE) }
    val acceptedContentType = request.headers[HttpHeaders.Accept]
    val receivedContentType = headers[HttpHeaders.ContentType]

    when (acceptedContentType) {
        receivedContentType, null -> return
    }

    val expectedContentType = ContentType.parse(acceptedContentType)

    if (expectedContentType == ContentType.Any) return

    val actualContentType = receivedContentType?.let { ContentType.parse(it) }
    if (actualContentType?.match(expectedContentType) ?: false) return

    onContentTypeMismatch(acceptedContentType, receivedContentType)
}
