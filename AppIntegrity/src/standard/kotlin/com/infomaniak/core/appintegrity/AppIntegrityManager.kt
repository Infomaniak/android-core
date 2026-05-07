/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2026 Infomaniak Network SA
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
package com.infomaniak.core.appintegrity

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.infomaniak.core.appintegrity.exceptions.AppIntegrityException
import com.infomaniak.core.appintegrity.exceptions.NetworkException
import com.infomaniak.core.appintegrity.exceptions.UnexpectedApiErrorFormatException
import com.infomaniak.core.common.cancellable
import com.infomaniak.core.sentry.SentryLog
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.tasks.await
import splitties.init.appCtx
import java.util.UUID

/**
 * Manager used to verify that the device used is real and doesn't have integrity problems
 *
 * There is 2 types of Request:
 * - the standard request ([requestIntegrityVerdictToken]) that need a warm-up first ([warmUpTokenProvider])
 * - the classic request ([requestClassicIntegrityVerdictToken]) that need additional API checks
 */
class AppIntegrityManager(private val appContext: Context, userAgent: String) : AbstractAppIntegrityManager() {

    private var appIntegrityTokenProvider: StandardIntegrityTokenProvider? = null
    private val classicIntegrityTokenProvider by lazy { IntegrityManagerFactory.create(appContext) }
    private val appIntegrityRepository by lazy { AppIntegrityRepository(userAgent) }

    private var challengeId = ""

    /**
     * This function is needed in case of standard verdict request by [requestIntegrityVerdictToken].
     * It must be called once at the initialisation because it can take a long time (up to several minutes)
     */
    override fun warmUpTokenProvider(appCloudNumber: Long, onFailure: () -> Unit) {
        val integrityManager = IntegrityManagerFactory.createStandard(appContext)
        integrityManager.prepareIntegrityToken(
            PrepareIntegrityTokenRequest.builder().setCloudProjectNumber(appCloudNumber).build()
        ).addOnSuccessListener { tokenProvider ->
            appIntegrityTokenProvider = tokenProvider
            SentryLog.i(APP_INTEGRITY_MANAGER_TAG, "warmUpTokenProvider: Success")
        }.addOnFailureListener { manageException(it, "Error during warmup", onFailure) }
    }

    /**
     * Standard verdict request for Integrity token
     * It should protect automatically from replay attack, but for now this protection seemed to not be working
     */
    override fun requestIntegrityVerdictToken(
        requestHash: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit,
        onNullTokenProvider: (String) -> Unit,
    ) {
        if (appIntegrityTokenProvider == null) {
            onNullTokenProvider("Integrity token provider is null during a verdict request. This should not be possible")
        } else {
            appIntegrityTokenProvider?.request(StandardIntegrityTokenRequest.builder().setRequestHash(requestHash).build())
                ?.addOnSuccessListener { response -> onSuccess(response.token()) }
                ?.addOnFailureListener { manageException(it, "Error when requiring a standard integrity token", onFailure) }
        }
    }

    override suspend fun isGuaranteedToFail(): Boolean {
        val isRunningGrapheneOS = try {
            appCtx.packageManager.getPackageInfo("app.grapheneos.info", PackageManager.MATCH_DISABLED_COMPONENTS)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
        if (isRunningGrapheneOS) return true

        // Be sure the length is between 16 and 500 char so the computed nonce sent to AppIntegrity will have a correct size
        val dummyChallenge = "Dummy challenge to know if AppIntegrity can be reached"
        return try {
            requestClassicIntegrityVerdictToken(dummyChallenge)
            false
        } catch (e: AppIntegrityException) {
            !e.issue.isRecoverable()
        } catch (t: Throwable) {
            SentryLog.e(APP_INTEGRITY_MANAGER_TAG, "Unexpected exception in isGuaranteedToFail", t)
            true
        }
    }

    /**
     * Classic verdict request for Integrity token
     *
     * This doesn't automatically protect from replay attack, thus the use of challenge/challengeId pair with our API to add this
     * layer of protection.
     *
     * ###### Can throw Integrity exceptions.
     */
    private suspend fun requestClassicIntegrityVerdictToken(challenge: String): IntegrityTokenResponse? {
        /**
         * The backend token check is disabled by default in preprod.
         * See [AppIntegrityRepository.getJwtToken]'s `force_integrity_test` if you want to enable it.
         */
        if (BuildConfig.DEBUG) return null

        val nonce = Base64.encodeToString(challenge.toByteArray(), Base64.DEFAULT)

        return runCatching {
            val tokenRequest = IntegrityTokenRequest.builder().setNonce(nonce).build()
            classicIntegrityTokenProvider.requestIntegrityToken(tokenRequest).await()
        }.cancellable().getOrElse { exception ->
            throw when (exception) {
                is IntegrityServiceException -> exception.toAppIntegrityException(classicIntegrityTokenProvider)
                else -> exception
            }
        }
    }

    override suspend fun getChallenge(): String {
        generateChallengeId()
        val apiResponse = appIntegrityRepository.getChallenge(challengeId)
        SentryLog.d(
            tag = APP_INTEGRITY_MANAGER_TAG,
            msg = "challengeId hash : ${challengeId.hashCode()} / challenge hash: ${apiResponse.data.hashCode()}",
        )
        return apiResponse.data ?: error("Get challenge cannot contain null data")
    }

    override suspend fun requestAttestationToken(
        challenge: String,
        packageName: String,
        targetUrl: String
    ): String {
        val tokenResponse = requestClassicIntegrityVerdictToken(challenge)
        return getApiIntegrityVerdict(tokenResponse, packageName, targetUrl)
    }

    private suspend fun getApiIntegrityVerdict(
        integrityToken: IntegrityTokenResponse?,
        packageName: String,
        targetUrl: String,
    ): String {
        runCatching {
            val apiResponse = appIntegrityRepository.getJwtToken(
                integrityToken = integrityToken?.token() ?: "fake integrity token",
                packageName = packageName,
                targetUrl = targetUrl,
                challengeId = challengeId,
            )
            return apiResponse.data ?: error("Integrity ApiResponse cannot contain null data")
        }.cancellable().getOrElse { exception ->
            //TODO[AppIntegrity]: Handle verdict issues fully by looking up backend response.
            // See this doc: https://developer.android.com/google/play/integrity/remediation#request-integrity-dialog
            if (exception is UnexpectedApiErrorFormatException && exception.bodyResponse.contains("invalid_attestation")) {
                throw AppIntegrityException(
                    errorCode = exception.statusCode,
                    issue = AppIntegrityIssue.SuspiciousError("invalid_attestation"),
                    message = "Invalid attestation",
                    cause = exception
                )
            } else {
                throw exception
            }
        }
    }

    /**
     *  Only used to test App Integrity in Apps before their real backend implementation
     */
    override suspend fun callDemoRoute(mobileToken: String) {
        runCatching {
            val apiResponse = appIntegrityRepository.demo(mobileToken)
            val logMessage = if (apiResponse.isSuccess()) {
                "Success demo route response: ${apiResponse.data}"
            } else {
                "Error demo route : ${apiResponse.error?.errorCode}"
            }
            Log.d(APP_INTEGRITY_MANAGER_TAG, logMessage)
        }.cancellable().getOrElse {
            it.printStackTrace()
        }
    }

    private fun generateChallengeId() {
        challengeId = UUID.randomUUID().toString()
    }

    private fun manageException(exception: Throwable, errorMessage: String, onFailure: () -> Unit) {
        if (exception !is NetworkException) {
            Sentry.captureMessage(errorMessage, SentryLevel.ERROR) { scope ->
                scope.setTag("exception", exception.message.toString())
                scope.setExtra("stacktrace", exception.printStackTrace().toString())
            }
        }
        SentryLog.w(APP_INTEGRITY_MANAGER_TAG, errorMessage, throwable = exception)
        onFailure()
    }

    companion object {
        const val APP_INTEGRITY_MANAGER_TAG = "App integrity manager"
        const val ATTESTATION_TOKEN_HEADER = "Ik-mobile-token"
    }
}
