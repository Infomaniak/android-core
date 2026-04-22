/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.network.utils.apiResponse

import com.infomaniak.core.network.models.ApiError
import com.infomaniak.core.network.models.ApiResponse
import io.sentry.Sentry

inline fun <T> ApiResponse<T>.onSuccess(block: T.() -> Unit): ApiResponse<T> = apply {
    if (isSuccess()) {
        data?.run(block) ?: Sentry.captureException(ApiResponseException(this))
    }
}

inline fun <T> ApiResponse<T>.onError(block: ApiError.() -> Unit): ApiResponse<T> = apply {
    if (isError()) {
        error?.run(block) ?: Sentry.captureException(ApiErrorException(this))
    }
}

inline fun <T> ApiResponse<T>.on(
    onSuccess: T.() -> Unit,
    onError: ApiError.() -> Unit
): ApiResponse<T> = onSuccess(onSuccess).onError(onError)

inline fun <T, R> ApiResponse<T>.mapSuccess(block: T.() -> R): R? {
    return takeIf { isSuccess() }
        ?.runCatching { data?.run(block) ?: throw ApiResponseException(this) }
        ?.onFailure(Sentry::captureException)
        ?.getOrNull()
}

inline fun <T, R> ApiResponse<T>.mapError(block: ApiError.() -> R): R? {
    return takeIf { isError() }
        ?.runCatching { error?.run(block) ?: throw ApiErrorException(this) }
        ?.onFailure(Sentry::captureException)
        ?.getOrNull()
}

inline fun <T, R> ApiResponse<T>.map(
    onSuccess: T.() -> R,
    onError: ApiError.() -> R
): R? = mapSuccess(onSuccess) ?: mapError(onError)
