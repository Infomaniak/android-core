/*
 * Infomaniak Core - Android
 * Copyright (C) 2021 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.auth

import com.infomaniak.lib.core.utils.ApiController
import com.infomaniak.lib.login.ApiToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenInterceptorListener: TokenInterceptorListener
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking(Dispatchers.IO) {
            mutex.withLock {
                val request = response.request
                val authorization = request.header("Authorization")
                var apiToken = tokenInterceptorListener.getApiToken()

                if (apiToken.accessToken != authorization?.replaceFirst("Bearer ", "")) {
                    return@runBlocking changeAccessToken(request, apiToken)
                } else {
                    apiToken = ApiController.refreshToken(apiToken.refreshToken, tokenInterceptorListener)
                    return@runBlocking changeAccessToken(request, apiToken)
                }
            }
        }
    }

    companion object {
        val mutex = Mutex()

        fun changeAccessToken(request: Request, apiToken: ApiToken): Request {
            val builder = request.newBuilder()
            builder.header("Authorization", "Bearer ${apiToken.accessToken}")
            return builder.build()
        }
    }
}