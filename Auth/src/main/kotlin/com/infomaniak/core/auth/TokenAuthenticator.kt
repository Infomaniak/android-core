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
package com.infomaniak.core.auth

import com.infomaniak.core.auth.api.AuthRepository
import com.infomaniak.core.auth.utils.ApiTokenExt.isInfinite
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

    private val userId = tokenInterceptorListener.getCurrentUserId()

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking(Dispatchers.IO) {
            mutex.withLock {
                val request = response.request
                val authorization = request.header("Authorization")
                val apiToken = tokenInterceptorListener.getApiToken() ?: return@runBlocking null
                val isAlreadyRefreshed = apiToken.accessToken != authorization?.replaceFirst("Bearer ", "")
                val hasUserChanged = userId != tokenInterceptorListener.getCurrentUserId()

                return@runBlocking when {
                    hasUserChanged -> null
                    apiToken.isInfinite -> {
                        tokenInterceptorListener.onRefreshTokenError()
                        null
                    }
                    isAlreadyRefreshed -> changeAccessToken(request, apiToken.accessToken)
                    else -> {
                        val newToken = AuthRepository.refreshToken(apiToken.refreshToken!!, tokenInterceptorListener)
                        changeAccessToken(request, newToken.accessToken)
                    }
                }
            }
        }
    }

    companion object {
        val mutex = Mutex()

        fun changeAccessToken(request: Request, accessToken: String): Request {
            val builder = request.newBuilder()
            builder.header("Authorization", "Bearer $accessToken")
            return builder.build()
        }
    }
}
