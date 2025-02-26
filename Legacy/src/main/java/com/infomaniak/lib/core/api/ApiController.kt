/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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

import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.infomaniak.lib.core.BuildConfig.LOGIN_ENDPOINT_URL
import com.infomaniak.lib.core.InfomaniakCore
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.auth.TokenInterceptorListener
import com.infomaniak.lib.core.models.ApiError
import com.infomaniak.lib.core.models.ApiResponse
import com.infomaniak.lib.core.models.ApiResponseStatus
import com.infomaniak.lib.core.models.ApiResponseStatus.ERROR
import com.infomaniak.lib.core.networking.HttpClient
import com.infomaniak.lib.core.networking.HttpUtils
import com.infomaniak.lib.core.utils.CustomDateTypeAdapter
import com.infomaniak.lib.core.utils.isNetworkException
import com.infomaniak.lib.login.ApiToken
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Type
import java.net.UnknownHostException
import java.util.Date
import kotlin.reflect.typeOf
import com.google.gson.JsonElement as GsonElement

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
        noinline buildErrorResult: ((apiError: ApiError?, translatedErrorRes: Int) -> T)? = null,
    ): T {
        val requestBody: RequestBody = generateRequestBody(body)
        return executeRequest(url, method, requestBody, okHttpClient, useKotlinxSerialization, buildErrorResult)
    }

    fun generateRequestBody(body: Any?): RequestBody {
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val toJson = when (body) {
            is JsonElement, is GsonElement -> body.toString()
            is String -> body
            else -> gson.toJson(body)
        }
        return toJson.toRequestBody(jsonMediaType)
    }

    suspend fun refreshToken(refreshToken: String, tokenInterceptorListener: TokenInterceptorListener): ApiToken {

        if (refreshToken.isBlank()) {
            tokenInterceptorListener.onRefreshTokenError()
            throw RefreshTokenException()
        }

        val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("grant_type", "refresh_token")
            .addFormDataPart("client_id", InfomaniakCore.clientId)
            .addFormDataPart("refresh_token", refreshToken)

        if (InfomaniakCore.accessType == null) formBuilder.addFormDataPart("duration", "infinite")

        val request = Request.Builder()
            .url("${LOGIN_ENDPOINT_URL}token")
            .post(formBuilder.build())
            .build()

        val apiToken = HttpClient.okHttpClientNoTokenInterceptor.newCall(request).execute().use {
            val bodyResponse = it.body?.string()

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

    class RefreshTokenException : Exception()

    inline fun <reified T> executeRequest(
        url: String,
        method: ApiMethod,
        requestBody: RequestBody,
        okHttpClient: OkHttpClient,
        useKotlinxSerialization: Boolean,
        noinline buildErrorResult: ((apiError: ApiError?, translatedErrorRes: Int) -> T)?,
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
                        createErrorResponse(
                            apiError = createApiError(useKotlinxSerialization, bodyResponse, ServerErrorException(bodyResponse)),
                            translatedError = R.string.serverError,
                            buildErrorResult = buildErrorResult,
                        )
                    }
                    bodyResponse.isBlank() -> createInternetErrorResponse(buildErrorResult = buildErrorResult)
                    else -> createApiResponse<T>(useKotlinxSerialization, bodyResponse, response.code)
                }
            }
        } catch (refreshTokenException: RefreshTokenException) {
            refreshTokenException.printStackTrace()
            return createErrorResponse(translatedError = R.string.anErrorHasOccurred, buildErrorResult = buildErrorResult)
        } catch (exception: Exception) {
            exception.printStackTrace()

            return if (exception.isNetworkException()) {
                createInternetErrorResponse(noNetwork = exception is UnknownHostException, buildErrorResult = buildErrorResult)
            } else {
                createErrorResponse(
                    translatedError = R.string.anErrorHasOccurred,
                    apiError = createApiError(useKotlinxSerialization, bodyResponse, exception = exception),
                    buildErrorResult = buildErrorResult,
                )
            }

        }
    }

    inline fun <reified T> createApiResponse(useKotlinxSerialization: Boolean, bodyResponse: String, codeResponse: Int): T {
        val apiResponse = when {
            useKotlinxSerialization -> json.decodeFromString<T>(bodyResponse)
            isApiResponseOfByteArray<T>() -> wrapByteArrayResponse<T>(bodyResponse, codeResponse)
            else -> gson.fromJson(bodyResponse, object : TypeToken<T>() {}.type)
        }

        if (apiResponse is ApiResponse<*> && apiResponse.result == ERROR) {
            apiResponse.translatedError = R.string.anErrorHasOccurred
        }

        return apiResponse
    }

    inline fun <reified T> wrapByteArrayResponse(bodyResponse: String, code: Int): T {
        return ApiResponse<ByteArray>(
            result = if (code > 200) ApiResponseStatus.UNKNOWN else ApiResponseStatus.SUCCESS,
            data = bodyResponse.toByteArray()
        ) as T
    }

    inline fun <reified T> isApiResponseOfByteArray(): Boolean {
        return typeOf<T>() == typeOf<ApiResponse<ByteArray>>()
    }

    private fun String.bodyResponseToJson(): JsonObject {
        return JsonObject(mapOf("bodyResponse" to JsonPrimitive(this)))
    }

    inline fun <reified T> createInternetErrorResponse(
        noNetwork: Boolean = false,
        noinline buildErrorResult: ((apiError: ApiError?, translatedErrorRes: Int) -> T)?,
    ) = createErrorResponse<T>(
        translatedError = if (noNetwork) R.string.noConnection else R.string.connectionError,
        apiError = ApiError(exception = NetworkException()),
        buildErrorResult = buildErrorResult,
    )

    fun createApiError(useKotlinxSerialization: Boolean, bodyResponse: String, exception: Exception) = ApiError(
        contextJson = if (useKotlinxSerialization) bodyResponse.bodyResponseToJson() else null,
        contextGson = when {
            useKotlinxSerialization -> null
            else -> runCatching { JsonParser.parseString(bodyResponse).asJsonObject }.getOrDefault(null)
        },
        exception = exception
    )

    inline fun <reified T> createErrorResponse(
        @StringRes translatedError: Int,
        apiError: ApiError? = null,
        noinline buildErrorResult: ((apiError: ApiError?, translatedErrorRes: Int) -> T)?,
    ): T {
        return buildErrorResult?.invoke(apiError, translatedError)
            ?: ApiResponse<Any>(result = ERROR, error = apiError, translatedError = translatedError) as T
    }

    class NetworkException : Exception()
    class ServerErrorException(val bodyResponse: String) : Exception(bodyResponse)

    enum class ApiMethod {
        GET, PUT, POST, DELETE, PATCH
    }
}
