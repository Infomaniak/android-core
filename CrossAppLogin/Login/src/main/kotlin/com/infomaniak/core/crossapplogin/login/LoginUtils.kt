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
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import com.infomaniak.core.auth.CredentialManager
import com.infomaniak.core.auth.TokenAuthenticator.Companion.changeAccessToken
import com.infomaniak.core.auth.api.ApiRepositoryCore
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.network.api.ApiController.toApiError
import com.infomaniak.core.network.api.InternalTranslatedErrorCode
import com.infomaniak.core.network.models.ApiResponse
import com.infomaniak.core.network.models.ApiResponseStatus
import com.infomaniak.core.network.utils.ApiErrorCode.Companion.translateError
import com.infomaniak.lib.login.ApiToken
import com.infomaniak.lib.login.InfomaniakLogin
import com.infomaniak.lib.login.InfomaniakLogin.ErrorStatus
import com.infomaniak.lib.login.InfomaniakLogin.TokenResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import okhttp3.OkHttpClient


object LoginUtils {
    /**
     * @return a LoginResult or null if the user canceled the login webview without going through to the end
     */
    suspend fun logUserInAfterWebView(
        result: ActivityResult,
        context: Context,
        infomaniakLogin: InfomaniakLogin,
        okHttpClient: OkHttpClient,
        credentialManager: CredentialManager,
        apiRepository: ApiRepositoryCore,
    ): LoginResult? {
        val authCodeResult = result.toAuthCodeResult(context)
        when (authCodeResult) {
            is AuthCodeResult.Error -> return LoginResult.Failure(authCodeResult.message)
            is AuthCodeResult.Canceled -> return null
            is AuthCodeResult.Success -> Unit
        }

        val tokenResult = infomaniakLogin.getToken(okHttpClient = okHttpClient, code = authCodeResult.code)
        when (tokenResult) {
            is TokenResult.Error -> {
                return LoginResult.Failure(context.getUserAuthenticationErrorMessage(tokenResult.errorStatus))
            }
            is TokenResult.Success -> Unit
        }

        val userResult = authenticateUsers(listOf(tokenResult.apiToken), okHttpClient, credentialManager, apiRepository).single()
        return when (userResult) {
            is UserResult.Failure -> {
                LoginResult.Failure(context.getString(userResult.apiResponse.translateError()))
            }
            is UserResult.Success -> LoginResult.Success(userResult.user)
        }
    }

    suspend fun logUserInAfterCrossAppLogin(
        apiTokens: List<ApiToken>,
        context: Context,
        okHttpClient: OkHttpClient,
        credentialManager: CredentialManager,
        apiRepository: ApiRepositoryCore,
    ): List<LoginResult> {
        return buildList {
            authenticateUsers(apiTokens, okHttpClient, credentialManager, apiRepository).forEach { result ->
                when (result) {
                    is UserResult.Success -> add(LoginResult.Success(result.user))
                    is UserResult.Failure -> add(LoginResult.Failure(context.getString(result.apiResponse.translateError())))
                }
            }
        }
    }

    private suspend fun authenticateUsers(
        apiTokens: List<ApiToken>,
        okHttpClient: OkHttpClient,
        credentialManager: CredentialManager,
        apiRepository: ApiRepositoryCore,
    ): List<UserResult> = apiTokens.map { apiToken ->
        runCatching {
            authenticateUser(apiToken, okHttpClient, credentialManager, apiRepository)
        }.getOrDefault(UserResult.Failure.Unknown)
    }

    private fun ActivityResult.toAuthCodeResult(context: Context): AuthCodeResult {
        if (resultCode != AppCompatActivity.RESULT_OK) return AuthCodeResult.Canceled

        val authCode = data?.getStringExtra(InfomaniakLogin.CODE_TAG)
        val translatedError = data?.getStringExtra(InfomaniakLogin.ERROR_TRANSLATED_TAG)

        return when {
            translatedError?.isNotBlank() == true -> AuthCodeResult.Error(translatedError)
            authCode?.isNotBlank() == true -> AuthCodeResult.Success(authCode)
            else -> AuthCodeResult.Error(context.getString(com.infomaniak.core.legacy.R.string.anErrorHasOccurred))
        }
    }

    private fun getErrorResponse(error: InternalTranslatedErrorCode): ApiResponse<Unit> {
        return ApiResponse(result = ApiResponseStatus.ERROR, error = error.toApiError())
    }

    private suspend fun authenticateUser(
        apiToken: ApiToken,
        okHttpClient: OkHttpClient,
        credentialManager: CredentialManager,
        apiRepository: ApiRepositoryCore,
    ): UserResult {
        if (credentialManager.getUserById(apiToken.userId) != null) return UserResult.Failure(
            getErrorResponse(InternalTranslatedErrorCode.UserAlreadyPresent)
        )

        val okhttpClient = okHttpClient.newBuilder().addInterceptor { chain ->
            val newRequest = changeAccessToken(chain.request(), apiToken)
            chain.proceed(newRequest)
        }.build()

        val userProfileResponse = Dispatchers.IO { apiRepository.getUserProfile(okhttpClient) }

        if (userProfileResponse.result == ApiResponseStatus.ERROR) return UserResult.Failure(userProfileResponse)
        if (userProfileResponse.data == null) return UserResult.Failure.Unknown

        val user = userProfileResponse.data!!.apply {
            this.apiToken = apiToken
            this.organizations = arrayListOf()
        }

        return UserResult.Success(user)
    }
}

private fun Context.getUserAuthenticationErrorMessage(errorStatus: ErrorStatus): String {
    return getString(
        when (errorStatus) {
            ErrorStatus.SERVER -> com.infomaniak.core.legacy.R.string.serverError
            ErrorStatus.CONNECTION -> com.infomaniak.core.legacy.R.string.connectionError
            else -> com.infomaniak.core.legacy.R.string.anErrorHasOccurred
        }
    )
}

sealed interface LoginResult {
    data class Success(val user: User) : LoginResult
    data class Failure(val errorMessage: String) : LoginResult
}
