/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2025 Infomaniak Network SA
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
package com.infomaniak.core.network.utils

import androidx.annotation.StringRes
import com.infomaniak.core.network.NetworkConfiguration
import com.infomaniak.core.network.api.InternalTranslatedErrorCode
import com.infomaniak.core.network.models.ApiResponse

interface ErrorCodeTranslated {
    val code: String
    @get:StringRes
    val translateRes: Int
}

data class ApiErrorCode(override val code: String, @StringRes override val translateRes: Int) : ErrorCodeTranslated {
    companion object {
        @StringRes
        fun <T> ApiResponse<T>.translateError(defaultMessage: Int? = null): Int {
            val error = formatError()
            return if (error == InternalTranslatedErrorCode.UnknownError && defaultMessage != null) {
                defaultMessage
            } else {
                error.translateRes
            }
        }

        fun <T> ApiResponse<T>.formatError(): ErrorCodeTranslated {
            val errorCode = error?.code
            return if (errorCode == null) {
                InternalTranslatedErrorCode.UnknownError
            } else {
                NetworkConfiguration.apiErrorCodes?.firstOrNull { it.code.equals(errorCode, ignoreCase = true) }
                    ?: InternalTranslatedErrorCode.entries.firstOrNull { it.code.equals(errorCode, ignoreCase = true) }
                    ?: InternalTranslatedErrorCode.UnknownError
            }
        }
    }
}
