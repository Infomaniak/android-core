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
package com.infomaniak.core.appintegrity.exceptions

import android.app.Activity
import com.infomaniak.core.appintegrity.AppIntegrityIssue
import com.infomaniak.core.appintegrity.IntegrityDialogResponse
import com.infomaniak.core.appintegrity.isRecoverable

/**
 * Thrown when either the Play App Integrity API or our remote API fails at verifying the device or the app integrity.
 *
 * - Some issues are recoverable, use the [isRecoverable] extension on [issue] to know.
 * - Some issues are remediable through a dialog. Use [showRemediationDialog] to do so.
 */
class AppIntegrityException(
    errorCode: Int,
    val issue: AppIntegrityIssue,
    message: String = "AppIntegrity issue ($errorCode): $issue",
    val showRemediationDialog: (suspend (Activity) -> IntegrityDialogResponse)? = null,
    cause: Throwable?,
) : Exception(message, cause)
