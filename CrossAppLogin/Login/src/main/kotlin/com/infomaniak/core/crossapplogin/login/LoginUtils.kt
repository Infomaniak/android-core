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
package com.infomaniak.core.crossapplogin.login

import android.content.Context
import com.infomaniak.core.auth.CredentialManager
import com.infomaniak.core.login.LoginUtils
import com.infomaniak.core.login.models.UserLoginResult
import com.infomaniak.core.login.models.UserResult
import com.infomaniak.core.network.utils.ApiErrorCode.Companion.translateError
import com.infomaniak.lib.login.ApiToken

/**
 * Logs the user based on the [ApiToken]s returned by the cross app login logic.
 *
 * @return a [UserLoginResult] or null if the user canceled the login webview without going through to the end
 */
suspend fun LoginUtils.getLoginResultsAfterCrossApp(
    apiTokens: List<ApiToken>,
    context: Context,
    credentialManager: CredentialManager,
): List<UserLoginResult> = buildList {
    authenticateUsers(apiTokens, credentialManager).forEach { result ->
        when (result) {
            is UserResult.Success -> add(UserLoginResult.Success(result.user))
            is UserResult.Failure -> add(UserLoginResult.Failure(context.getString(result.apiResponse.translateError())))
        }
    }
}
