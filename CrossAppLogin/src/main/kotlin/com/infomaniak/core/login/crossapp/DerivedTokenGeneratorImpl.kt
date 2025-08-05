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
package com.infomaniak.core.login.crossapp

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.infomaniak.core.DynamicLazyMap
import com.infomaniak.core.Xor
import com.infomaniak.core.appintegrity.AppIntegrityManager
import com.infomaniak.core.appintegrity.AppIntegrityManager.Companion.APP_INTEGRITY_MANAGER_TAG
import com.infomaniak.core.appintegrity.exceptions.IntegrityException
import com.infomaniak.core.appintegrity.exceptions.NetworkException
import com.infomaniak.core.cancellable
import com.infomaniak.core.login.crossapp.DerivedTokenGenerator.Issue
import com.infomaniak.lib.core.utils.SentryLog
import com.infomaniak.lib.core.utils.await
import com.infomaniak.lib.login.ApiToken
import com.infomaniak.lib.login.InfomaniakLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import splitties.init.appCtx
import java.io.IOException

class DerivedTokenGeneratorImpl(
    coroutineScope: CoroutineScope,
    private val tokenRetrievalUrl: String,
    private val hostAppPackageName: String,
    private val clientId: String,
    private val userAgent: String,
    private val accessType: InfomaniakLogin.AccessType? = null,
) : DerivedTokenGenerator {

    private val appIntegrityManager = AppIntegrityManager(appCtx, userAgent)

    private val attestationTokensForUrls = DynamicLazyMap<String, Deferred<Xor<String, Issue>>>(
        cacheManager = { _, asyncResult ->
            // TODO: Cache tokens again (forever only if possible) once they can be reused,
            //  by uncommenting, the line below, and possibly replacing awaitCancellation() if reuse is limited in time.
            // if (asyncResult.await() is Xor.First) awaitCancellation()
            // Skip cache if unsuccessful.
        },
        coroutineScope = coroutineScope
    ) { targetUrl -> async { attemptFetchNewAttestationToken(targetUrl) } }

    override suspend fun attemptDerivingOneOfTheseTokens(tokensToTry: Set<String>): Xor<ApiToken, Issue> {
        require(tokensToTry.isNotEmpty())

        return tokensToTry.map { tokenToDerive ->
            when (val result = attemptDerivingToken(tokenToDerive)) {
                is Xor.First -> return result
                is Xor.Second -> result
            }
        }.last()
    }

    private suspend fun attemptDerivingToken(token: String): Xor<ApiToken, Issue> {
        val targetUrl = tokenRetrievalUrl
        val attestationToken: String = when (val result = attestationTokensForUrls.useElement(targetUrl) { it.await() }) {
            is Xor.First -> result.value
            is Xor.Second -> return result
        }
        val form = MultipartBody.Builder().also {
            it.setType(MultipartBody.FORM)
            it.addFormDataPart("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
            it.addFormDataPart("subject_token", token)
            it.addFormDataPart("subject_token_type", "urn:ietf:params:oauth:token-type:access_token")
            it.addFormDataPart("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
            it.addFormDataPart("client_assertion", attestationToken)
            it.addFormDataPart("client_id", clientId)
            if (accessType == null) it.addFormDataPart("duration", "infinite")
        }.build()

        val okHttpClient = OkHttpClient()

        return getToken(okHttpClient, targetUrl, form)
    }

    private val gson: Gson by lazy { Gson() }

    private suspend fun getToken(
        okHttpClient: OkHttpClient,
        url: String,
        body: RequestBody
    ): Xor<ApiToken, Issue> = runCatching {

        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("User-Agent", userAgent)
            .build()

        val response = okHttpClient.newCall(request).await()
        val bodyResponse = response.body?.string()

        return if (response.isSuccessful) {
            val jsonResult = JsonParser.parseString(bodyResponse)
            val apiToken = gson.fromJson(jsonResult, ApiToken::class.java)

            // Set the token expiration date (with margin-delay)
            apiToken.expiresAt = System.currentTimeMillis() + ((apiToken.expiresIn - 60) * 1000)

            Xor.First(apiToken)
        } else {
            Xor.Second(Issue.ErrorResponse(response.code))
        }
    }.cancellable().getOrElse {
        val issue: Issue = when (it) {
            is IOException, is NetworkException -> Issue.NetworkIssue(it)
            else -> Issue.OtherIssue(it)
        }
        Xor.Second(issue)
    }

    private suspend fun attemptFetchNewAttestationToken(targetUrl: String): Xor<String, Issue> = runCatching {
        Xor.First(fetchNewAttestationToken(targetUrl))
    }.cancellable().getOrElse {
        val issue: Issue = when (it) {
            is IntegrityException -> Issue.AppIntegrityCheckFailed(it)
            is IOException, is NetworkException -> Issue.NetworkIssue(it)
            else -> Issue.OtherIssue(it)
        }
        Xor.Second(issue)
    }

    // TODO: Improve error handling as some are recoverable (network or backends availability related), while some are not.
    //  See Play Integrity error codes: https://developer.android.com/google/play/integrity/error-codes,
    //  and remediation: https://developer.android.com/google/play/integrity/remediation
    @Throws(IntegrityException::class)
    private suspend inline fun fetchNewAttestationToken(targetUrl: String): String {
        val challenge = appIntegrityManager.getChallenge()

        val appIntegrityToken = appIntegrityManager.requestClassicIntegrityVerdictToken(challenge)
        SentryLog.i(APP_INTEGRITY_MANAGER_TAG, "request for app integrity token successful")

        val attestationToken = appIntegrityManager.getApiIntegrityVerdict(
            integrityToken = appIntegrityToken,
            packageName = hostAppPackageName,
            targetUrl = targetUrl,
        )
        SentryLog.i(APP_INTEGRITY_MANAGER_TAG, "Successful API verdict")

        return attestationToken
    }
}
