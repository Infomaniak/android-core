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
package com.infomaniak.lib.core.models

import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.RawValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class ApiResponse<T>(
    val result: ApiResponseStatus = ApiResponseStatus.UNKNOWN,
    val data: @RawValue T? = null,
    val uri: String? = null,
    var error: ApiError? = null,
    val page: Int = 0,
    val pages: Int = 0,
    @SerialName("response_at")
    @SerializedName("response_at")
    val responseAt: Long = 0,
    val total: Int = 0,
    @Deprecated(
        "translatedError doesn't take into account project specific translated errors specified through " +
                "InfomaniakCore.apiErrorCodes. Use translateError() instead",
        ReplaceWith("translateError()", "com.infomaniak.lib.core.utils.ApiErrorCode.Companion.translateError"),
    )
    @StringRes
    @Transient
    var translatedError: Int = 0,
    @SerialName("items_per_page")
    @SerializedName("items_per_page")
    val itemsPerPage: Int = 0,
) {

    fun isSuccess() = result == ApiResponseStatus.SUCCESS
}
