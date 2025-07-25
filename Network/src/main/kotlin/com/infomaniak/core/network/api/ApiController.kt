/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.core.network.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.infomaniak.core.network.isNetworkException
import com.infomaniak.core.network.isSerializationException
import com.infomaniak.core.network.models.ApiError
import com.infomaniak.core.network.models.ApiResponse
import com.infomaniak.core.network.models.ApiResponseStatus
import com.infomaniak.core.network.networking.HttpClient
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.network.networking.ManualAuthorizationRequired
import com.infomaniak.core.network.utils.CustomDateTypeAdapter
import com.infomaniak.core.network.utils.ErrorCodeTranslated
import com.infomaniak.core.network.utils.await
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Type
import java.net.UnknownHostException
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

    suspend inline fun <reified T> callApi(
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
            is JsonElement, is com.google.gson.JsonElement -> body.toString()
            is String -> body
            else -> gson.toJson(body)
        }
        return toJson.toRequestBody(jsonMediaType)
    }

    class RefreshTokenException : Exception()

    /**
     * `executeRequest()` & `executeRequestBlocking()` are mostly duplicated.
     * If one is modified, the other should too.
     */
    suspend inline fun <reified T> executeRequest(
        url: String,
        method: ApiMethod,
        requestBody: RequestBody,
        okHttpClient: OkHttpClient,
        useKotlinxSerialization: Boolean,
        noinline buildErrorResult: ((apiError: ApiError?, translatedErrorRes: Int) -> T)?,
    ): T {
        var bodyResponse = ""
        try {
            val request = createRequest(url, method, requestBody)

            okHttpClient.newCall(request).await().use { response ->
                bodyResponse = response.body?.string() ?: ""
                return when {
                    response.code >= 500 -> {
                        Sentry.captureMessage("An API error ${response.code} occurred", SentryLevel.ERROR) { scope ->
                            scope.setExtra("bodyResponse", bodyResponse)
                        }
                        createErrorResponse(
                            InternalTranslatedErrorCode.UnknownError,
                            InternalErrorPayload(ServerErrorException(bodyResponse), useKotlinxSerialization, bodyResponse),
                            buildErrorResult = buildErrorResult,
                        )
                    }
                    bodyResponse.isBlank() -> createInternetErrorResponse(buildErrorResult = buildErrorResult)
                    else -> createApiResponse<T>(useKotlinxSerialization, bodyResponse)
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (refreshTokenException: RefreshTokenException) {
            refreshTokenException.printStackTrace()
            return createErrorResponse(InternalTranslatedErrorCode.UnknownError, buildErrorResult = buildErrorResult)
        } catch (exception: Exception) {
            exception.printStackTrace()

            return if (exception.isNetworkException()) {
                createInternetErrorResponse(noNetwork = exception is UnknownHostException, buildErrorResult = buildErrorResult)
            } else {

                if (exception.isSerializationException()) {
                    Sentry.captureMessage("Error while decoding API Response", SentryLevel.ERROR) { scope ->
                        scope.setExtra("bodyResponse", bodyResponse)
                    }
                }

                createErrorResponse(
                    InternalTranslatedErrorCode.UnknownError,
                    InternalErrorPayload(exception, useKotlinxSerialization, bodyResponse),
                    buildErrorResult = buildErrorResult,
                )
            }
        }
    }

    @OptIn(ManualAuthorizationRequired::class)
    fun createRequest(
        url: String,
        method: ApiMethod,
        requestBody: RequestBody
    ): Request = Request.Builder()
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

    inline fun <reified T> createApiResponse(useKotlinxSerialization: Boolean, bodyResponse: String): T {
        val apiResponse = when {
            useKotlinxSerialization -> json.decodeFromString<T>(bodyResponse)
            else -> gson.fromJson(bodyResponse, object : TypeToken<T>() {}.type)
        }

        if (apiResponse is ApiResponse<*> && apiResponse.result == ApiResponseStatus.ERROR) {
            @Suppress("DEPRECATION")
            apiResponse.translatedError = InternalTranslatedErrorCode.UnknownError.translateRes
        }

        return apiResponse
    }

    private fun String.bodyResponseToJson(): JsonObject {
        return JsonObject(mapOf("bodyResponse" to JsonPrimitive(this)))
    }

    inline fun <reified T> createInternetErrorResponse(
        noNetwork: Boolean = false,
        noinline buildErrorResult: ((apiError: ApiError?, translatedErrorRes: Int) -> T)?,
    ): T = createErrorResponse<T>(
        if (noNetwork) InternalTranslatedErrorCode.NoConnection else InternalTranslatedErrorCode.ConnectionError,
        InternalErrorPayload(NetworkException()),
        buildErrorResult = buildErrorResult,
    )

    inline fun <reified T> createErrorResponse(
        internalErrorCode: InternalTranslatedErrorCode,
        payload: InternalErrorPayload? = null,
        noinline buildErrorResult: ((apiError: ApiError?, translatedErrorRes: Int) -> T)?,
    ): T {
        val apiError = createDetailedApiError(internalErrorCode, payload)
        val translatedError = internalErrorCode.translateRes

        return buildErrorResult?.invoke(apiError, translatedError)
            ?: ApiResponse<Any>(result = ApiResponseStatus.ERROR, error = apiError, translatedError = translatedError) as T
    }

    class NetworkException : Exception()
    class ServerErrorException(bodyResponse: String) : Exception(bodyResponse)

    enum class ApiMethod {
        GET, PUT, POST, DELETE, PATCH
    }

    data class InternalErrorPayload(
        val exception: Exception? = null,
        val useKotlinxSerialization: Boolean? = null,
        val bodyResponse: String? = null,
    )

    fun ErrorCodeTranslated.toApiError(): ApiError = createDetailedApiError(this, null)

    fun createDetailedApiError(errorCode: ErrorCodeTranslated, payload: InternalErrorPayload? = null): ApiError {
        val useKotlinxSerialization = payload?.useKotlinxSerialization
        return ApiError(
            code = errorCode.code,
            contextJson = if (useKotlinxSerialization == true) payload.bodyResponse?.bodyResponseToJson() else null,
            contextGson = when {
                useKotlinxSerialization == true -> null
                else -> errorCode.runCatching {
                    payload?.let { JsonParser.parseString(it.bodyResponse).asJsonObject }
                }.getOrDefault(null)
            },
            exception = payload?.exception
        )
    }
}
