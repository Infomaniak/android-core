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
package com.infomaniak.lib.core.utils

import androidx.annotation.StringRes
import com.infomaniak.lib.core.InfomaniakCore
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.api.ApiController.TranslatedInternalErrorCode
import com.infomaniak.lib.core.models.ApiResponse

interface ErrorCode {
    val code: String

    interface Translated : ErrorCode {
        override val code: String
        @get:StringRes
        val translateRes: Int
    }
}

data class ApiErrorCode(override val code: String, @StringRes override val translateRes: Int) : ErrorCode.Translated {

    companion object {

        const val AN_ERROR_HAS_OCCURRED = "an_error_has_occured"

        private val defaultApiErrorCode = ApiErrorCode(AN_ERROR_HAS_OCCURRED, R.string.anErrorHasOccurred)

        @StringRes
        fun <T> ApiResponse<T>.translateError(): Int = formatError().translateRes

        fun <T> ApiResponse<T>.formatError(): ErrorCode.Translated {
            val errorCode = error?.code
            return if (errorCode == null) {
                defaultApiErrorCode
            } else {
                InfomaniakCore.apiErrorCodes?.firstOrNull { it.code.equals(errorCode, ignoreCase = true) }
                    ?: TranslatedInternalErrorCode.entries.firstOrNull { it.code.equals(errorCode, ignoreCase = true) }
                    ?: defaultApiErrorCode
            }
        }
    }
}
