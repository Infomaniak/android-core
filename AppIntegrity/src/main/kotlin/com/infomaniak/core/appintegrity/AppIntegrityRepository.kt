/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.core.appintegrity

import com.infomaniak.core.appintegrity.AppIntegrityManager.Companion.ATTESTATION_TOKEN_HEADER
import com.infomaniak.core.appintegrity.exceptions.ApiException
import com.infomaniak.core.appintegrity.exceptions.NetworkException
import com.infomaniak.core.appintegrity.exceptions.UnexpectedApiErrorFormatException
import com.infomaniak.core.appintegrity.exceptions.UnknownException
import com.infomaniak.core.appintegrity.models.ApiResponse
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.Url
import io.ktor.http.contentType

internal class AppIntegrityRepository(userAgent: String) {

    private val apiClientProvider by lazy { ApiClientProvider(userAgent = userAgent) }

    suspend fun getChallenge(challengeId: String): ApiResponse<String> {
        val body = mapOf("challenge_id" to challengeId)
        return post<ApiResponse<String>>(url = Url(AppIntegrityRoutes.requestChallenge), data = body)
    }

    suspend fun getJwtToken(
        integrityToken: String,
        packageName: String,
        targetUrl: String,
        challengeId: String,
    ): ApiResponse<String> {
        val body = mutableMapOf(
            "token" to integrityToken,
            "package_name" to packageName,
            "target_url" to targetUrl,
            "challenge_id" to challengeId,
        )

        // Add this line to test validation by API
        // body["force_integrity_test"] = "true"

        return post<ApiResponse<String>>(url = Url(AppIntegrityRoutes.requestApiIntegrityCheck), data = body)
    }

    suspend fun demo(mobileToken: String): ApiResponse<String> {
        return post<ApiResponse<String>>(
            url = Url(AppIntegrityRoutes.demo),
            data = mapOf<String, String>(),
            appendHeaders = { append(ATTESTATION_TOKEN_HEADER, mobileToken) },
        )
    }

    private suspend inline fun <reified R> post(
        url: Url,
        data: Any?,
        crossinline appendHeaders: HeadersBuilder.() -> Unit = {},
    ): R {
        return apiClientProvider.httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { appendHeaders() }
            setBody(data)
        }.decode<R>()
    }

    private suspend inline fun <reified R> HttpResponse.decode(): R {
        return runCatching { body<R>() }.getOrElse { exception ->
            when (exception) {
                is ApiException, is NetworkException, is UnexpectedApiErrorFormatException -> throw exception
                else -> throw UnknownException(exception)
            }
        }
    }
}
