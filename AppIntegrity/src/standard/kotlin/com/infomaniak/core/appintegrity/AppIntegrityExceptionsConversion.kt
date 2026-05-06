/*
 * Infomaniak Authenticator - Android
 * Copyright (C) 2026 Infomaniak Network SA
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

import androidx.activity.ComponentActivity
import com.google.android.play.core.integrity.IntegrityDialogRequest
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityDialogRequest.StandardIntegrityResponse
import com.google.android.play.core.integrity.model.IntegrityDialogResponseCode
import com.google.android.play.core.integrity.model.IntegrityDialogTypeCode
import com.google.android.play.core.integrity.model.IntegrityErrorCode
import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode
import com.infomaniak.core.appintegrity.exceptions.AppIntegrityException
import kotlinx.coroutines.tasks.await

internal fun StandardIntegrityException.toAppIntegrityException(
    standardIntegrityManager: StandardIntegrityManager
): AppIntegrityException {
    return AppIntegrityException(
        errorCode = errorCode,
        issue = appIntegrityIssueFor(integrityErrorCode = errorCode),
        showRemediationDialog = if (isRemediable) showStandardRemediationDialog(this, standardIntegrityManager) else null,
        cause = this,
    )
}

internal fun IntegrityServiceException.toAppIntegrityException(
    standardIntegrityManager: IntegrityManager
): AppIntegrityException {
    return AppIntegrityException(
        errorCode = errorCode,
        issue = appIntegrityIssueFor(integrityErrorCode = errorCode),
        showRemediationDialog = if (isRemediable) showRemediationDialog(this, standardIntegrityManager) else null,
        cause = this,
    )
}

private fun showStandardRemediationDialog(
    exception: StandardIntegrityException,
    standardIntegrityManager: StandardIntegrityManager
): suspend (ComponentActivity) -> IntegrityDialogResponse = { activity ->
    val dialogRequest = StandardIntegrityManager.StandardIntegrityDialogRequest.builder()
        .setActivity(activity)
        .setTypeCode(IntegrityDialogTypeCode.GET_INTEGRITY)
        .setStandardIntegrityResponse(StandardIntegrityResponse.ExceptionDetails(exception))
        .build()

    val dialogResponseCode = standardIntegrityManager.showDialog(dialogRequest).await()
    remediationDialogResponseCodeToIntegrityDialogResponse(dialogResponseCode)
}

private fun showRemediationDialog(
    exception: IntegrityServiceException,
    integrityManager: IntegrityManager
): suspend (ComponentActivity) -> IntegrityDialogResponse = { activity ->
    val dialogRequest = IntegrityDialogRequest.builder()
        .setActivity(activity)
        .setTypeCode(IntegrityDialogTypeCode.GET_INTEGRITY)
        .setIntegrityResponse(IntegrityDialogRequest.IntegrityResponse.ExceptionDetails(exception))
        .build()

    val dialogResponseCode = integrityManager.showDialog(dialogRequest).await()
    remediationDialogResponseCodeToIntegrityDialogResponse(dialogResponseCode)
}

/**
 * See [the online doc](https://developer.android.com/google/play/integrity/reference/com/google/android/play/core/integrity/model/IntegrityDialogResponseCode)
 *
 * @see IntegrityDialogResponse
 */
private fun remediationDialogResponseCodeToIntegrityDialogResponse(code: Int): IntegrityDialogResponse = when (code) {
    IntegrityDialogResponseCode.DIALOG_CANCELLED -> IntegrityDialogResponse.Cancelled
    IntegrityDialogResponseCode.DIALOG_FAILED -> IntegrityDialogResponse.Failed
    IntegrityDialogResponseCode.DIALOG_UNAVAILABLE -> IntegrityDialogResponse.Unavailable
    IntegrityDialogResponseCode.DIALOG_SUCCESSFUL -> IntegrityDialogResponse.Successful
    else -> IntegrityDialogResponse.Failed
}

/**
 * See the [Handle Play Integrity API error codes](https://developer.android.com/google/play/integrity/error-codes) page.
 *
 * @see IntegrityErrorCode
 * @see StandardIntegrityErrorCode
 */
private fun appIntegrityIssueFor(
    @StandardIntegrityErrorCode @IntegrityErrorCode integrityErrorCode: Int
): AppIntegrityIssue = when (integrityErrorCode) {
    IntegrityErrorCode.NETWORK_ERROR -> AppIntegrityIssue.RetryLater.NetworkError
    IntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE -> AppIntegrityIssue.RetryLater.GoogleServerUnavailable
    IntegrityErrorCode.CLIENT_TRANSIENT_ERROR -> AppIntegrityIssue.Internal.ClientTransientError
    StandardIntegrityErrorCode.CLIENT_TRANSIENT_ERROR -> AppIntegrityIssue.Internal.ClientTransientError
    IntegrityErrorCode.TOO_MANY_REQUESTS -> AppIntegrityIssue.RetryLater.TooManyRequests
    IntegrityErrorCode.INTERNAL_ERROR -> AppIntegrityIssue.Internal.InternalError
    IntegrityErrorCode.CANNOT_BIND_TO_SERVICE -> AppIntegrityIssue.Internal.CantBindToService
    IntegrityErrorCode.PLAY_STORE_VERSION_OUTDATED -> AppIntegrityIssue.DeviceIssue.PlayStoreVersionOutdated
    IntegrityErrorCode.PLAY_SERVICES_VERSION_OUTDATED -> AppIntegrityIssue.DeviceIssue.PlayServicesVersionOutdated
    IntegrityErrorCode.PLAY_STORE_ACCOUNT_NOT_FOUND -> AppIntegrityIssue.DeviceIssue.PlayStoreAccountNotFound
    IntegrityErrorCode.PLAY_STORE_NOT_FOUND -> AppIntegrityIssue.DeviceIssue.PlayStoreNotFound
    IntegrityErrorCode.PLAY_SERVICES_NOT_FOUND -> AppIntegrityIssue.DeviceIssue.PlayServicesNotFound
    IntegrityErrorCode.API_NOT_AVAILABLE -> AppIntegrityIssue.DeviceIssue.ApiNotAvailable
    IntegrityErrorCode.CLOUD_PROJECT_NUMBER_IS_INVALID -> AppIntegrityIssue.DevError("CLOUD_PROJECT_NUMBER_IS_INVALID")
    IntegrityErrorCode.APP_NOT_INSTALLED -> AppIntegrityIssue.SuspiciousError("APP_NOT_INSTALLED")
    IntegrityErrorCode.APP_UID_MISMATCH -> AppIntegrityIssue.SuspiciousError("APP_UID_MISMATCH")
    IntegrityErrorCode.NONCE_TOO_SHORT -> AppIntegrityIssue.DevError("NONCE_TOO_SHORT")
    IntegrityErrorCode.NONCE_TOO_LONG -> AppIntegrityIssue.DevError("NONCE_TOO_LONG")
    IntegrityErrorCode.NONCE_IS_NOT_BASE64 -> AppIntegrityIssue.DevError("NONCE_IS_NOT_BASE64")
    IntegrityErrorCode.NO_ERROR -> AppIntegrityIssue.SuspiciousError("NO_ERROR (What a Terrible Failure)")
    StandardIntegrityErrorCode.INTEGRITY_TOKEN_PROVIDER_INVALID -> AppIntegrityIssue.Internal.IntegrityTokenProviderInvalid
    else -> AppIntegrityIssue.SuspiciousError("Unexpected error code: $integrityErrorCode")
}
