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
package com.infomaniak.core.twofactorauth.back

import com.infomaniak.core.cancellable
import com.infomaniak.core.network.LOGIN_ENDPOINT_URL
import com.infomaniak.core.network.models.ApiResponse
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.network.networking.ManualAuthorizationRequired
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.core.twofactorauth.back.TwoFactorAuth.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.IOException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
internal class TwoFactorAuthImpl(
    connectedHttpClient: OkHttpClient,
    override val userId: Int,
) : TwoFactorAuth {

    private val httpClient = HttpClient(OkHttp) {
        engine { preconfigured = connectedHttpClient }
        install(ContentNegotiation) {
            val jsonConfig = Json {
                /** From [io.ktor.serialization.kotlinx.json.DefaultJson] */
                encodeDefaults = true
                isLenient = true
                allowSpecialFloatingPointValues = true
                allowStructuredMapKeys = true
                prettyPrint = false
                useArrayPolymorphism = false

                // Use-case specific config:
                coerceInputValues = true // Use default values if not recognized (used for enums).
                ignoreUnknownKeys = true // Don't break if keys are added.
                @OptIn(ExperimentalSerializationApi::class)
                decodeEnumsCaseInsensitive = true
            }
            json(jsonConfig)
        }
        install(HttpRequestRetry) {
            maxRetries = 3
            retryOnExceptionIf { _, cause -> cause !is SerializationException }
        }
        defaultRequest {
            url("$LOGIN_ENDPOINT_URL/api/2fa/push/")
            userAgent(HttpUtils.getUserAgent)
            headers {
                @OptIn(ManualAuthorizationRequired::class) // Already handled by the http client.
                HttpUtils.getHeaders().forEach { (header, value) -> append(header, value) }
            }
        }
    }

    /**
     * Retrieves the latest login challenge, if any.
     *
     * Returns null otherwise, or in case of another issue (network or backend related).
     *
     * Might retry several times before returning.
     */
    override suspend fun tryGettingLatestChallenge(): RemoteChallenge? = runCatching {
        val response = httpClient.get("challenges")
        val challenge: RemoteChallenge? = when {
            response.status.isSuccess() -> response.body<ApiResponse<RemoteChallenge>>().data
            else -> {
                val httpCode = response.status.value
                SentryLog.e(TAG, "Failed to get the latest challenge [http $httpCode]")
                null
            }
        }
        when (challenge?.type) {
            RemoteChallenge.Type.Approval -> Unit
            null -> return null
        }
        challenge
    }.cancellable().getOrElse { t ->
        when (t) {
            is IOException -> SentryLog.i(TAG, "I/O issue while trying to get the latest challenge", t)
            else -> SentryLog.e(TAG, "Couldn't get the latest challenge because of an unknow issue", t)
        }
        null
    }

    override suspend fun approveChallenge(challengeUid: Uuid): Outcome = respondToChallenge(
        actionVerb = "approve",
        challengeUid = challengeUid,
    ) { httpClient.patch("challenges/" + challengeUid.toHexDashString()) }

    override suspend fun rejectChallenge(challengeUid: Uuid): Outcome = respondToChallenge(
        actionVerb = "reject",
        challengeUid = challengeUid,
    ) { httpClient.delete("challenges/" + challengeUid.toHexDashString()) }

    private suspend inline fun respondToChallenge(
        actionVerb: String,
        challengeUid: Uuid,
        operation: suspend () -> HttpResponse
    ): Outcome = runCatching {
        SentryLog.i(TAG, "Trying to $actionVerb the challenge ($challengeUid)")
        val response = operation()
        when {
            response.status.isSuccess() -> {
                SentryLog.i(TAG, "Attempt to $actionVerb the challenge succeeded! ($challengeUid)")
                Outcome.Done.Success
            }
            else -> when (val httpCode = response.status.value) {
                410 -> Outcome.Done.Expired
                404 -> Outcome.Done.AlreadyProcessed
                else -> {
                    val bodyString = runCatching { response.bodyAsText() }.cancellable().getOrNull()
                    SentryLog.e(TAG, "Failed to $actionVerb the challenge [http $httpCode] ($challengeUid) $bodyString")
                    Outcome.Issue.ErrorResponse
                }
            }
        }
    }.cancellable().getOrElse { t ->
        when (t) {
            is IOException -> {
                SentryLog.i(TAG, "I/O issue while trying to $actionVerb the challenge ($challengeUid)", t)
                Outcome.Issue.Network
            }
            else -> {
                SentryLog.e(TAG, "Couldn't $actionVerb the challenge ($challengeUid) because of an unknow issue", t)
                Outcome.Issue.Unknown
            }
        }
    }

    private companion object {
        const val TAG = "TwoFactorAuthImpl"
    }
}
