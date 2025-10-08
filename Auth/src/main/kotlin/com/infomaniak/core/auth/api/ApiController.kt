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
package com.infomaniak.core.auth.api

import com.google.gson.JsonParser
import com.infomaniak.core.auth.AuthConfiguration
import com.infomaniak.core.auth.TokenInterceptorListener
import com.infomaniak.core.network.LOGIN_ENDPOINT_URL
import com.infomaniak.core.network.api.ApiController.RefreshTokenException
import com.infomaniak.core.network.api.ApiController.gson
import com.infomaniak.core.network.networking.HttpClient
import com.infomaniak.core.network.utils.await
import com.infomaniak.core.network.utils.bodyAsStringOrNull
import com.infomaniak.lib.login.ApiToken
import okhttp3.MultipartBody
import okhttp3.Request

object ApiController {
    suspend fun refreshToken(refreshToken: String, tokenInterceptorListener: TokenInterceptorListener): ApiToken {

        if (refreshToken.isBlank()) {
            tokenInterceptorListener.onRefreshTokenError()
            throw RefreshTokenException()
        }

        val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.Companion.FORM)
            .addFormDataPart("grant_type", "refresh_token")
            .addFormDataPart("client_id", AuthConfiguration.clientId)
            .addFormDataPart("refresh_token", refreshToken)

        if (AuthConfiguration.accessType == null) formBuilder.addFormDataPart("duration", "infinite")

        val request = Request.Builder()
            .url("$LOGIN_ENDPOINT_URL/token")
            .post(formBuilder.build())
            .build()

        val apiToken = HttpClient.okHttpClient.newCall(request).await().use {
            val bodyResponse = it.bodyAsStringOrNull()

            when {
                it.isSuccessful -> {
                    getApiToken(bodyResponse, tokenInterceptorListener)
                }
                else -> {
                    handleInvalidGrant(bodyResponse, tokenInterceptorListener)
                    throw Exception(bodyResponse)
                }
            }
        }

        return apiToken
    }

    private suspend fun handleInvalidGrant(bodyResponse: String?, tokenInterceptorListener: TokenInterceptorListener) {
        val invalidGrant = runCatching {
            JsonParser.parseString(bodyResponse)
                .asJsonObject
                .getAsJsonPrimitive("error")
                .asString == "invalid_grant"
        }.getOrDefault(false)

        if (invalidGrant) {
            tokenInterceptorListener.onRefreshTokenError()
            throw RefreshTokenException()
        }
    }

    private suspend fun getApiToken(
        bodyResponse: String?,
        tokenInterceptorListener: TokenInterceptorListener,
    ): ApiToken {
        val apiToken = gson.fromJson(bodyResponse, ApiToken::class.java)
        apiToken.expiresAt = System.currentTimeMillis() + (apiToken.expiresIn * 1_000L)
        tokenInterceptorListener.onRefreshTokenSuccess(apiToken)
        return apiToken
    }
}
