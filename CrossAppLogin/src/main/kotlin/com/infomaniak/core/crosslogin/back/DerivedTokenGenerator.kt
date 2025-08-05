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
package com.infomaniak.core.crosslogin.back

import com.infomaniak.core.Xor
import com.infomaniak.core.appintegrity.exceptions.IntegrityException
import com.infomaniak.lib.login.ApiToken

sealed interface DerivedTokenGenerator {

    suspend fun attemptDerivingOneOfTheseTokens(tokensToTry: Set<String>): Xor<ApiToken, Issue>

    sealed interface Issue {
        data class ErrorResponse(val httpStatusCode: Int) : Issue
        data class NetworkIssue(val e: Exception) : Issue
        data class OtherIssue(val e: Throwable) : Issue
        data class AppIntegrityCheckFailed(val details: IntegrityException) : Issue
    }
}
