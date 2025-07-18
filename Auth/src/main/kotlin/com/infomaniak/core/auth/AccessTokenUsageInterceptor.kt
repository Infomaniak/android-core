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
package com.infomaniak.core.auth

import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.net.ssl.HttpsURLConnection

private var lastReportEpoch: Long? = null
private val lastReportMutex = Mutex()

class AccessTokenUsageInterceptor(
    private val previousApiCall: ApiCallRecord?,
    private val updateLastApiCall: (ApiCallRecord) -> Unit,
) : Interceptor {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.message != TokenInterceptor.USER_LOGGED_OUT_MESSAGE) {
            processAccessTokenUsageAsync(request, response.code)
        }

        return response
    }

    private fun processAccessTokenUsageAsync(request: Request, responseCode: Int) {
        coroutineScope.launch {

            val currentApiCall = ApiCallRecord(
                accessToken = request.header("Authorization")?.replaceFirst("Bearer ", "") ?: return@launch,
                date = System.currentTimeMillis() / 1_000L,
                responseCode = responseCode,
            )

            // Only report api calls that triggered an unauthorized response else record the call for future checks
            if (responseCode != HttpsURLConnection.HTTP_UNAUTHORIZED) {
                updateLastApiCall(currentApiCall)
                return@launch
            }

            // If multiple unauthorized calls are received at the same time, only send the first one to sentry
            lastReportMutex.withLock {
                val lastReport = lastReportEpoch
                if (lastReport != null && lastReport <= currentApiCall.date && currentApiCall.date - lastReport < TEN_SECONDS) {
                    return@launch
                }
                lastReportEpoch = currentApiCall.date
            }

            if (previousApiCall != null &&
                currentApiCall.accessToken == previousApiCall.accessToken &&
                currentApiCall.date < previousApiCall.date + SIX_MONTHS
            ) {
                Sentry.captureMessage(
                    "Got disconnected due to non-working access token but it's not been six months yet",
                    SentryLevel.FATAL,
                ) { scope ->
                    scope.setExtra("Current api call date epoch", currentApiCall.date.toString())
                    scope.setExtra("Current api call token", formatAccessTokenForSentry(currentApiCall.accessToken))

                    scope.setExtra("Last known api call date epoch", previousApiCall.date.toString())
                    scope.setExtra("Last known api call response code", previousApiCall.responseCode.toString())
                    scope.setExtra("Last known api call token", formatAccessTokenForSentry(previousApiCall.accessToken))
                }
            }
        }
    }

    private fun formatAccessTokenForSentry(accessToken: String): String = accessToken.take(2) + "..." + accessToken.takeLast(2)

    @Serializable
    data class ApiCallRecord(val accessToken: String, val date: Long, val responseCode: Int)

    companion object {
        private const val SECONDS_IN_A_DAY = 86_400L
        private const val SIX_MONTHS = SECONDS_IN_A_DAY * 182L // In seconds
        private const val TEN_SECONDS = 10 // In seconds
    }
}
