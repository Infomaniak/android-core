/*
 * Infomaniak Core - Android
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

import android.app.Activity
import com.google.android.play.core.integrity.IntegrityDialogRequest
import com.google.android.play.core.integrity.IntegrityDialogRequest.IntegrityResponse
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityDialogRequest.StandardIntegrityResponse
import com.google.android.play.core.integrity.model.IntegrityDialogResponseCode
import com.google.android.play.core.integrity.model.IntegrityDialogTypeCode
import com.google.android.play.core.integrity.model.IntegrityErrorCode
import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode
import com.infomaniak.core.appintegrity.AppIntegrityIssue.DevError
import com.infomaniak.core.appintegrity.AppIntegrityIssue.DeviceIssue
import com.infomaniak.core.appintegrity.AppIntegrityIssue.Internal
import com.infomaniak.core.appintegrity.AppIntegrityIssue.RetryLater
import com.infomaniak.core.appintegrity.AppIntegrityIssue.SuspiciousError
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

internal fun IntegrityTokenResponse.showRemediationDialog(
    integrityManager: IntegrityManager,
    typeCode: Int = IntegrityDialogTypeCode.GET_INTEGRITY
): suspend (Activity) -> IntegrityDialogResponse = { activity ->
    // See this doc: https://developer.android.com/google/play/integrity/remediation#request-integrity-dialog
    val dialogRequest = IntegrityDialogRequest.builder()
        .setActivity(activity)
        .setTypeCode(typeCode)
        .setIntegrityResponse(IntegrityResponse.TokenResponse(this))
        .build()

    val dialogResponseCode = integrityManager.showDialog(dialogRequest).await()
    remediationDialogResponseCodeToIntegrityDialogResponse(dialogResponseCode)
}

private fun showStandardRemediationDialog(
    exception: StandardIntegrityException,
    standardIntegrityManager: StandardIntegrityManager
): suspend (Activity) -> IntegrityDialogResponse = { activity ->
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
): suspend (Activity) -> IntegrityDialogResponse = { activity ->
    val dialogRequest = IntegrityDialogRequest.builder()
        .setActivity(activity)
        .setTypeCode(IntegrityDialogTypeCode.GET_INTEGRITY)
        .setIntegrityResponse(IntegrityResponse.ExceptionDetails(exception))
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
    IntegrityErrorCode.NETWORK_ERROR -> RetryLater.NetworkError
    IntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE -> RetryLater.GoogleServerUnavailable
    IntegrityErrorCode.CLIENT_TRANSIENT_ERROR -> Internal.ClientTransientError
    StandardIntegrityErrorCode.CLIENT_TRANSIENT_ERROR -> Internal.ClientTransientError
    IntegrityErrorCode.TOO_MANY_REQUESTS -> RetryLater.TooManyRequests
    IntegrityErrorCode.INTERNAL_ERROR -> Internal.InternalError
    IntegrityErrorCode.CANNOT_BIND_TO_SERVICE -> Internal.CantBindToService
    IntegrityErrorCode.PLAY_STORE_VERSION_OUTDATED -> DeviceIssue.PlayStoreVersionOutdated
    IntegrityErrorCode.PLAY_SERVICES_VERSION_OUTDATED -> DeviceIssue.PlayServicesVersionOutdated
    IntegrityErrorCode.PLAY_STORE_ACCOUNT_NOT_FOUND -> DeviceIssue.PlayStoreAccountNotFound
    IntegrityErrorCode.PLAY_STORE_NOT_FOUND -> DeviceIssue.PlayStoreNotFound
    IntegrityErrorCode.PLAY_SERVICES_NOT_FOUND -> DeviceIssue.PlayServicesNotFound
    IntegrityErrorCode.API_NOT_AVAILABLE -> DeviceIssue.ApiNotAvailable
    IntegrityErrorCode.CLOUD_PROJECT_NUMBER_IS_INVALID -> DevError("CLOUD_PROJECT_NUMBER_IS_INVALID")
    IntegrityErrorCode.APP_NOT_INSTALLED -> SuspiciousError("APP_NOT_INSTALLED")
    IntegrityErrorCode.APP_UID_MISMATCH -> SuspiciousError("APP_UID_MISMATCH")
    IntegrityErrorCode.NONCE_TOO_SHORT -> DevError("NONCE_TOO_SHORT")
    IntegrityErrorCode.NONCE_TOO_LONG -> DevError("NONCE_TOO_LONG")
    IntegrityErrorCode.NONCE_IS_NOT_BASE64 -> DevError("NONCE_IS_NOT_BASE64")
    IntegrityErrorCode.NO_ERROR -> SuspiciousError("NO_ERROR (What a Terrible Failure)")
    StandardIntegrityErrorCode.INTEGRITY_TOKEN_PROVIDER_INVALID -> Internal.IntegrityTokenProviderInvalid
    else -> SuspiciousError("Unexpected error code: $integrityErrorCode")
}
