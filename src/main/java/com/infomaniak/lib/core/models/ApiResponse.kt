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
package com.infomaniak.lib.core.models

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.RawValue

data class ApiResponse<T>(
    val result: Status = Status.UNKNOWN,
    val data: @RawValue T? = null,
    val error: ApiError? = null,
    val page: Int = 0,
    val pages: Int = 0,
    @SerializedName("response_at") val responseAt: Long = 0,
    val total: Int = 0,
    var translatedError: Int = 0,
    @SerializedName("items_per_page") val itemsPerPage: Int = 0
) {

    fun isSuccess() = result == Status.SUCCESS

    enum class Status {

        @SerializedName("error")
        ERROR,

        @SerializedName("success")
        SUCCESS,

        @SerializedName("asynchronous")
        ASYNCHRONOUS,

        @SerializedName("unknown")
        UNKNOWN;
    }
}