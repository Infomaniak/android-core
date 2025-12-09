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
package com.infomaniak.core.auth.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.infomaniak.core.auth.TokenAuthenticator.Companion.changeAccessToken
import com.infomaniak.core.auth.UserExistenceChecker
import com.infomaniak.core.auth.api.ApiRepositoryCore
import com.infomaniak.core.auth.models.AuthCodeResult
import com.infomaniak.core.auth.models.UserLoginResult
import com.infomaniak.core.auth.models.UserResult
import com.infomaniak.core.cancellable
import com.infomaniak.core.network.api.ApiController.toApiError
import com.infomaniak.core.network.api.InternalTranslatedErrorCode
import com.infomaniak.core.network.models.ApiResponse
import com.infomaniak.core.network.models.ApiResponseStatus
import com.infomaniak.core.network.networking.HttpClient
import com.infomaniak.core.network.utils.ApiErrorCode.Companion.translateError
import com.infomaniak.lib.login.ApiToken
import com.infomaniak.lib.login.InfomaniakLogin
import com.infomaniak.lib.login.InfomaniakLogin.ErrorStatus
import com.infomaniak.lib.login.InfomaniakLogin.TokenResult

object LoginUtils {
    /**
     * Logs the user based on the ActivityResult of our login WebView.
     *
     * @return a [UserLoginResult] or null if the user canceled the login webview without going through to the end
     */
    suspend fun getLoginResultAfterWebView(
        result: ActivityResult,
        context: Context,
        infomaniakLogin: InfomaniakLogin,
        userExistenceChecker: UserExistenceChecker,
    ): UserLoginResult? {
        val authCodeResult = result.toAuthCodeResult(context)
        when (authCodeResult) {
            is AuthCodeResult.Error -> return UserLoginResult.Failure(authCodeResult.message)
            is AuthCodeResult.Canceled -> return null
            is AuthCodeResult.Success -> Unit
        }

        val tokenResult = infomaniakLogin.getToken(okHttpClient = HttpClient.okHttpClient, code = authCodeResult.code)
        when (tokenResult) {
            is TokenResult.Error -> return UserLoginResult.Failure(context.formatAuthErrorMessage(tokenResult.errorStatus))
            is TokenResult.Success -> Unit
        }

        val userResult = getUsersByToken(listOf(tokenResult.apiToken), userExistenceChecker).single()
        return when (userResult) {
            is UserResult.Failure -> {
                UserLoginResult.Failure(context.getString(userResult.apiResponse.translateError()))
            }
            is UserResult.Success -> UserLoginResult.Success(userResult.user)
        }
    }

    /**
     * Logs the user based on the [ApiToken]s returned by the cross app login logic.
     *
     * @return a [UserLoginResult] or null if the user canceled the login webview without going through to the end
     */
    suspend fun getLoginResultsAfterCrossApp(
        apiTokens: List<ApiToken>,
        context: Context,
        userExistenceChecker: UserExistenceChecker,
    ): List<UserLoginResult> = getUsersByToken(apiTokens, userExistenceChecker).map { result ->
        when (result) {
            is UserResult.Success -> UserLoginResult.Success(result.user)
            is UserResult.Failure -> UserLoginResult.Failure(context.getString(result.apiResponse.translateError()))
        }
    }

    /**
     * Automatically logs in the user using [loginResultLauncher] after a successful account creation or return an error result.
     */
    fun getAccountCreationResult(
        result: ActivityResult,
        infomaniakLogin: InfomaniakLogin,
        loginResultLauncher: ActivityResultLauncher<Intent>,
    ): AccountCreationResult {
        if (result.resultCode != AppCompatActivity.RESULT_OK) return AccountCreationResult.Canceled

        val translatedError = result.data?.getStringExtra(InfomaniakLogin.ERROR_TRANSLATED_TAG)
        return when {
            translatedError.isNullOrBlank() -> {
                infomaniakLogin.startWebViewLogin(resultLauncher = loginResultLauncher, removeCookies = false)
                AccountCreationResult.Success
            }
            else -> AccountCreationResult.Failure(translatedError)
        }
    }
}

/**
 * This method is the basis on which [getLoginResultAfterWebView] and its alternative [getLoginResultsAfterCrossApp] are built.
 */
private suspend fun getUsersByToken(
    apiTokens: List<ApiToken>,
    userExistenceChecker: UserExistenceChecker,
): List<UserResult> = apiTokens.map { apiToken ->
    runCatching {
        authenticateUser(apiToken, userExistenceChecker)
    }.cancellable().getOrDefault(UserResult.Failure.Unknown)
}

private fun ActivityResult.toAuthCodeResult(context: Context): AuthCodeResult {
    if (resultCode != AppCompatActivity.RESULT_OK) return AuthCodeResult.Canceled

    val authCode = data?.getStringExtra(InfomaniakLogin.CODE_TAG)
    val translatedError = data?.getStringExtra(InfomaniakLogin.ERROR_TRANSLATED_TAG)

    return when {
        translatedError?.isNotBlank() == true -> AuthCodeResult.Error(translatedError)
        authCode?.isNotBlank() == true -> AuthCodeResult.Success(authCode)
        else -> AuthCodeResult.Error(context.getString(InternalTranslatedErrorCode.UnknownError.translateRes))
    }
}

private fun Context.formatAuthErrorMessage(errorStatus: ErrorStatus): String {
    return getString(
        when (errorStatus) {
            ErrorStatus.SERVER -> InternalTranslatedErrorCode.ServerError
            ErrorStatus.CONNECTION -> InternalTranslatedErrorCode.ConnectionError
            else -> InternalTranslatedErrorCode.UnknownError
        }.translateRes
    )
}

private suspend fun authenticateUser(apiToken: ApiToken, userExistenceChecker: UserExistenceChecker): UserResult {
    if (userExistenceChecker.isUserAlreadyPresent(apiToken.userId)) return UserResult.Failure(
        getErrorResponse(InternalTranslatedErrorCode.UserAlreadyPresent)
    )

    val okhttpClient = HttpClient.okHttpClient.newBuilder().addInterceptor { chain ->
        val newRequest = changeAccessToken(chain.request(), apiToken)
        chain.proceed(newRequest)
    }.build()

    val userProfileResponse = ApiRepositoryCore.getUserProfile(okhttpClient)

    if (userProfileResponse.result == ApiResponseStatus.ERROR) return UserResult.Failure(userProfileResponse)
    val userData = userProfileResponse.data ?: return UserResult.Failure.Unknown

    val user = userData.apply {
        this.apiToken = apiToken
        this.organizations = arrayListOf()
    }

    return UserResult.Success(user)
}

private fun getErrorResponse(error: InternalTranslatedErrorCode): ApiResponse<Unit> {
    return ApiResponse(result = ApiResponseStatus.ERROR, error = error.toApiError())
}
