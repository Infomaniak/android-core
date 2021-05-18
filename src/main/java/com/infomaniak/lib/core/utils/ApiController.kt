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
package com.infomaniak.lib.core.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.infomaniak.lib.core.BuildConfig.LOGIN_ENDPOINT_URL
import com.infomaniak.lib.core.InfomaniakCore

import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.auth.TokenInterceptorListener
import com.infomaniak.lib.core.models.ApiResponse
import com.infomaniak.lib.core.models.ApiResponse.Status.ERROR
import com.infomaniak.lib.core.networking.HttpClient
import com.infomaniak.lib.core.networking.HttpUtils
import com.infomaniak.lib.login.ApiToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.cli.MissingArgumentException
import java.lang.reflect.Type
import java.util.*

object ApiController {

    var gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, CustomDateTypeAdapter())
        .create()

    fun init(typeAdapterList: ArrayList<Pair<Type, Any>>) {
        val gsonBuilder = gson.newBuilder()

        for (typeAdapter in typeAdapterList) {
            gsonBuilder.registerTypeAdapter(typeAdapter.first, typeAdapter.second)
        }

        gson = gsonBuilder.create()
    }

    inline fun <reified T> callApi(
        url: String,
        method: ApiMethod,
        body: Any? = null,
        okHttpClient: OkHttpClient = HttpClient.okHttpClient
    ): T {
        val requestBody: RequestBody = generateRequestBody(body)
        return executeRequest(url, method, requestBody, okHttpClient)
    }

    fun generateRequestBody(body: Any?): RequestBody {
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val toJson = if (body is JsonElement) body.toString() else gson.toJson(body)
        return toJson.toRequestBody(jsonMediaType)
    }

    suspend fun refreshToken(refreshToken: String, tokenInterceptorListener: TokenInterceptorListener): ApiToken {
        val clientID = InfomaniakCore.clientID ?: throw MissingArgumentException("clientId core not initialized")
        val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("grant_type", "refresh_token")
            .addFormDataPart("client_id", clientID)
            .addFormDataPart("refresh_token", refreshToken)

        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${LOGIN_ENDPOINT_URL}token")
                .post(formBuilder.build())
                .build()

            val response = HttpClient.okHttpClientNoInterceptor.newCall(request).execute()
            val bodyResponse = response.body?.string()

            when {
                response.isSuccessful -> {
                    val apiToken = gson.fromJson(bodyResponse, ApiToken::class.java)
                    apiToken.expiresAt = System.currentTimeMillis() + (apiToken.expiresIn * 1000)
                    tokenInterceptorListener.onRefreshTokenSuccess(apiToken)
                    return@withContext apiToken
                }
                else -> {
                    var invalidGrant = false
                    try {
                        invalidGrant = JsonParser.parseString(bodyResponse)
                            .asJsonObject
                            .getAsJsonPrimitive("error")
                            .asString == "invalid_grant"
                    } catch (_: Exception) {
                    }

                    if (invalidGrant) {
                        tokenInterceptorListener.onRefreshTokenError()
                        throw RefreshTokenException()
                    }
                    throw Exception(bodyResponse)
                }
            }
        }
    }

    class RefreshTokenException : Exception()

    inline fun <reified T> executeRequest(
        url: String,
        method: ApiMethod,
        requestBody: RequestBody,
        okHttpClient: OkHttpClient
    ): T {
        try {
            val request = Request.Builder()
                .url(url)
                .headers(HttpUtils.getHeaders())
                .apply {
                    when (method) {
                        ApiMethod.GET -> get()
                        ApiMethod.POST -> post(requestBody)
                        ApiMethod.DELETE -> delete(requestBody)
                        ApiMethod.PUT -> put(requestBody)
                    }
                }
                .build()

            val response = okHttpClient.newCall(request).execute()
            val bodyResponse = response.body?.string()

            return when {
                response.code >= 500 -> {
                    ApiResponse<Any>(result = ERROR, translatedError = R.string.serverError) as T
                }
                bodyResponse.isNullOrBlank() -> {
                    ApiResponse<Any>(result = ERROR, translatedError = R.string.connectionError) as T
                }
                else -> {
                    return gson.fromJson<T>(bodyResponse, object : TypeToken<T>() {}.type).apply {
                        if (this is ApiResponse<*>) {
                            val apiResponse = this as ApiResponse<*>
                            if (apiResponse.result == ERROR) {
                                apiResponse.translatedError = R.string.anErrorHasOccurred
                            }
                        }
                    }
                }
            }
        } catch (refreshTokenException: RefreshTokenException) {
            refreshTokenException.printStackTrace()
            return ApiResponse<Any>(result = ERROR, translatedError = R.string.anErrorHasOccurred) as T
        } catch (exception: Exception) {
            exception.printStackTrace()

            val descriptionError = if (exception.javaClass.name.contains("java.net.", ignoreCase = true) ||
                exception.javaClass.name.contains("javax.net.", ignoreCase = true) ||
                exception.javaClass.name.contains("java.io.", ignoreCase = true)
            ) {
                R.string.connectionError
            } else {
                R.string.anErrorHasOccurred
            }
            return ApiResponse<Any>(result = ERROR, translatedError = descriptionError) as T
        }
    }

    enum class ApiMethod {
        GET, PUT, POST, DELETE
    }
}