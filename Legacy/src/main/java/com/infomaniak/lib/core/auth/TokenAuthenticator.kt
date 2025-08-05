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

import com.infomaniak.lib.core.api.ApiController
import com.infomaniak.lib.core.utils.ApiTokenExt.isInfinite
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

    private val userId = tokenInterceptorListener.getCurrentUserId()

    override fun authenticate(route: Route?, response: Response): Request? {
        val hasUserChanged = userId != tokenInterceptorListener.getCurrentUserId()
        if (hasUserChanged) return null

        return runBlocking(Dispatchers.IO) {
            val userApiToken = tokenInterceptorListener.getUserApiToken() ?: return@runBlocking null
            if (userApiToken.isInfinite) {
                tokenInterceptorListener.onRefreshTokenError()
                null
            } else {
                mutex.withLock {
                    val request = response.request
                    val apiCallBearer = request.header("Authorization")?.replaceFirst("Bearer ", "")
                    val isAlreadyRefreshed = userApiToken.accessToken != apiCallBearer

                    return@runBlocking when {
                        isAlreadyRefreshed -> changeAccessToken(request, userApiToken)
                        else -> {
                            val newToken =
                                ApiController.refreshToken(userApiToken.refreshToken!!, tokenInterceptorListener)
                            changeAccessToken(request, newToken)
                        }
                    }
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
