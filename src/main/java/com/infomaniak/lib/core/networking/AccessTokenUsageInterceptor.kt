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
package com.infomaniak.lib.core.networking

import com.infomaniak.lib.core.auth.TokenInterceptorListener
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import okhttp3.Interceptor
import okhttp3.Response

class AccessTokenUsageInterceptor(
    private val tokenInterceptorListener: TokenInterceptorListener,
    private val previousApiCall: ApiCallRecord?,
    private val updateLastApiCall: (ApiCallRecord) -> Unit,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        runBlocking(Dispatchers.IO) {
            // Only log api calls if we're not using refresh tokens
            if (tokenInterceptorListener.getApiToken().refreshToken != null) return@runBlocking

            val currentApiCall = ApiCallRecord(
                accessToken = request.header("Authorization")?.replaceFirst("Bearer ", "") ?: return@runBlocking,
                date = System.currentTimeMillis() / 1000,
            )

            if (response.code != 401) {
                updateLastApiCall(currentApiCall)
                return@runBlocking
            }

            if (previousApiCall != null
                && currentApiCall.accessToken == previousApiCall.accessToken
                && currentApiCall.date < previousApiCall.date + ONE_YEAR
            ) {
                Sentry.withScope { scope ->
                    scope.level = SentryLevel.ERROR

                    scope.setExtra("last known api call date epoch", previousApiCall.date.toString())
                    scope.setExtra("last known api call token", formatAccessTokenForSentry(previousApiCall.accessToken))

                    scope.setExtra("current api call date epoch", currentApiCall.date.toString())
                    scope.setExtra("current api call token", formatAccessTokenForSentry(currentApiCall.accessToken))

                    Sentry.captureMessage("Got disconnected with non working access token but it's not been a year yet")
                }
            }

            updateLastApiCall(currentApiCall)
        }

        return response
    }

    private fun formatAccessTokenForSentry(accessToken: String): String = accessToken.take(2) + "..." + accessToken.takeLast(2)

    @Serializable
    data class ApiCallRecord(val accessToken: String, val date: Long)

    companion object {
        private const val ONE_YEAR = 60 * 60 * 24 * 365
    }
}
