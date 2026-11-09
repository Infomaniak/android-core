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

inline fun <T : Any> ApiResponse<T>.onSuccess(block: T.() -> Unit): ApiResponse<T> = apply {
    if (isSuccess()) {
        data?.run(block) ?: Sentry.captureException(ApiResponseException(this))
    }
}

inline fun <T : Any> ApiResponse<T>.onError(block: ApiError.() -> Unit): ApiResponse<T> = apply {
    if (isError()) {
        error?.run(block) ?: Sentry.captureException(ApiErrorException(this))
    }
}

inline fun <T : Any> ApiResponse<T>.on(
    onSuccess: T.() -> Unit,
    onError: ApiError.() -> Unit
): ApiResponse<T> = onSuccess(onSuccess).onError(onError)
