/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
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
package com.infomaniak.lib.core.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.infomaniak.lib.core.BuildConfig.LOGIN_ENDPOINT_URL
import com.infomaniak.lib.core.InfomaniakCore
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.auth.TokenInterceptorListener
import com.infomaniak.lib.core.models.ApiError
import com.infomaniak.lib.core.models.ApiResponse
import com.infomaniak.lib.core.models.ApiResponse.Status.ERROR
import com.infomaniak.lib.core.networking.HttpClient
import com.infomaniak.lib.core.networking.HttpUtils
import com.infomaniak.lib.core.utils.CustomDateTypeAdapter
import com.infomaniak.lib.core.utils.isNetworkException
import com.infomaniak.lib.login.ApiToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.cli.MissingArgumentException
import java.lang.reflect.Type
import java.util.Date

object ApiController {

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

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
        okHttpClient: OkHttpClient = HttpClient.okHttpClient,
        useKotlinxSerialization: Boolean = false,
    ): T {
        val requestBody: RequestBody = generateRequestBody(body)
        return executeRequest(url, method, requestBody, okHttpClient, useKotlinxSerialization)
    }

    fun generateRequestBody(body: Any?): RequestBody {
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val toJson = when (body) {
            is JsonElement -> body.toString()
            is String -> body
            else -> gson.toJson(body)
        }
        return toJson.toRequestBody(jsonMediaType)
    }

    suspend fun refreshToken(refreshToken: String, tokenInterceptorListener: TokenInterceptorListener): ApiToken {
        val clientId = InfomaniakCore.clientId ?: throw MissingArgumentException("clientId core not initialized")
        val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("grant_type", "refresh_token")
            .addFormDataPart("client_id", clientId)
            .addFormDataPart("refresh_token", refreshToken)

        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${LOGIN_ENDPOINT_URL}token")
                .post(formBuilder.build())
                .build()

            val response = HttpClient.okHttpClientNoInterceptor.newCall(request).execute()
            response.use {
                val bodyResponse = it.body?.string()

                when {
                    it.isSuccessful -> {
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
    }

    class RefreshTokenException : Exception()

    inline fun <reified T> executeRequest(
        url: String,
        method: ApiMethod,
        requestBody: RequestBody,
        okHttpClient: OkHttpClient,
        useKotlinxSerialization: Boolean,
    ): T {
        var bodyResponse = ""
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
                        ApiMethod.PATCH -> patch(requestBody)
                    }
                }
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                bodyResponse = response.body?.string() ?: ""
                return when {
                    response.code >= 500 -> {
                        ApiResponse<Any>(
                            result = ERROR,
                            error = ApiError(context = bodyResponse.bodyResponseToJson(), exception = ServerErrorException()),
                            translatedError = R.string.serverError
                        ) as T
                    }
                    bodyResponse.isBlank() -> getApiResponseInternetError()
                    else -> {
                        val decodedApiResponse = if (useKotlinxSerialization) {
                            json.decodeFromString<T>(bodyResponse)
                        } else {
                            gson.fromJson(bodyResponse, object : TypeToken<T>() {}.type)
                        }
                        decodedApiResponse.apply {
                            val apiResponse = this as? ApiResponse<*>
                            if (apiResponse?.result == ERROR) apiResponse.translatedError = R.string.anErrorHasOccurred
                        }
                    }
                }
            }
        } catch (refreshTokenException: RefreshTokenException) {
            refreshTokenException.printStackTrace()
            return ApiResponse<Any>(result = ERROR, translatedError = R.string.anErrorHasOccurred) as T
        } catch (exception: Exception) {
            exception.printStackTrace()

            return if (exception.isNetworkException()) {
                getApiResponseInternetError()
            } else {
                ApiResponse<Any>(
                    result = ERROR,
                    error = ApiError(context = bodyResponse.bodyResponseToJson(), exception = exception),
                    translatedError = R.string.anErrorHasOccurred
                ) as T
            }

        }
    }

    fun String.bodyResponseToJson(): JsonObject {
        return JsonObject(mapOf("bodyResponse" to JsonPrimitive(this)))
    }

    inline fun <reified T> getApiResponseInternetError() = ApiResponse<Any>(
        result = ERROR,
        error = ApiError(exception = NetworkException()),
        translatedError = R.string.connectionError
    ) as T

    class NetworkException : Exception()
    class ServerErrorException : Exception()

    enum class ApiMethod {
        GET, PUT, POST, DELETE, PATCH
    }
}
