/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2026 Infomaniak Network SA
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
package com.infomaniak.core.legacy.utils

import androidx.annotation.StringRes
import com.infomaniak.core.legacy.InfomaniakCore
import com.infomaniak.core.legacy.api.InternalTranslatedErrorCode
import com.infomaniak.core.legacy.models.ApiResponse

@Deprecated("Use the one from core.network.utils in Core:Network module")
interface ErrorCodeTranslated {
    val code: String
    @get:StringRes
    val translateRes: Int
}

@Deprecated("Use the one from core.network.utils in Core:Network module")
data class ApiErrorCode(override val code: String, @StringRes override val translateRes: Int) : ErrorCodeTranslated {
    companion object {
        @Deprecated("Use the one from core.network.utils in Core:Network module")
        @StringRes
        fun <T> ApiResponse<T>.translateError(): Int = formatError().translateRes

        @Deprecated("Use the one from core.network.utils in Core:Network module")
        fun <T> ApiResponse<T>.formatError(): ErrorCodeTranslated {
            val errorCode = error?.code
            return if (errorCode == null) {
                InternalTranslatedErrorCode.UnknownError
            } else {
                InfomaniakCore.apiErrorCodes?.firstOrNull { it.code.equals(errorCode, ignoreCase = true) }
                    ?: InternalTranslatedErrorCode.entries.firstOrNull { it.code.equals(errorCode, ignoreCase = true) }
                    ?: InternalTranslatedErrorCode.UnknownError
            }
        }
    }
}
