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
package com.infomaniak.lib.core.auth

import com.infomaniak.lib.core.api.ApiController.json
import com.infomaniak.lib.core.api.ApiController.toApiError
import com.infomaniak.lib.core.api.InternalTranslatedErrorCode
import com.infomaniak.lib.core.auth.TokenAuthenticator.Companion.changeAccessToken
import com.infomaniak.lib.core.models.ApiResponse
import com.infomaniak.lib.core.models.ApiResponseStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class TokenInterceptor(
    private val tokenInterceptorListener: TokenInterceptorListener
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        runBlocking(Dispatchers.Default) {
            tokenInterceptorListener.getApiToken()
        }?.let { apiToken ->
            val authorization = request.header("Authorization")
            if (apiToken.accessToken != authorization?.replaceFirst("Bearer ", "")) {
                request = changeAccessToken(request, apiToken)
            }
        } ?: return userLoggedOutResponse(chain)

        return chain.proceed(request)
    }

    private fun userLoggedOutResponse(chain: Interceptor.Chain): Response {
        val errorBody = ApiResponse<Unit>(
            result = ApiResponseStatus.ERROR,
            error = InternalTranslatedErrorCode.UserLoggedOut.toApiError()
        )
        return Response.Builder()
            .code(401)
            .request(chain.request())
            .protocol(Protocol.HTTP_2)
            .message("User is logged out")
            .body(json.encodeToString(errorBody).toResponseBody())
            .build()
    }
}
