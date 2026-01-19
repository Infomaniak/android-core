/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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
package com.infomaniak.core.crossapplogin.back

import com.infomaniak.core.common.service.ServiceBindingIssue
import com.infomaniak.core.sentry.SentryLog
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal object IpcIssuesManagerImpl : IpcIssuesManager {

    private val connectionTimeoutUserWaiting = 7.seconds
    private val connectionTimeoutBackground = 10.seconds

    private const val TAG = "IpcIssuesManager"

    override fun timeoutConnection(
        targetPackageName: String,
        isUserWaiting: Boolean
    ): suspend (Boolean) -> Unit = { isWaitingForReconnect: Boolean ->
        val timeout = if (isUserWaiting) connectionTimeoutUserWaiting else connectionTimeoutBackground
        delay(timeout)
        SentryLog.i(TAG, "Waited $timeout for connection to $targetPackageName")
    }

    override suspend fun timeoutOperation(
        operationName: String,
        targetPackageName: String,
        acceptableDuration: Duration
    ) {
        val timeout = acceptableDuration
        delay(timeout)
        SentryLog.i(TAG, "Waited $timeout for IPC with $targetPackageName for $operationName")
        SentryLog.w(TAG, "Timing out IPC with $targetPackageName for $operationName")
    }

    override fun logBindingIssue(
        targetPackageName: String,
        issue: ServiceBindingIssue
    ) = when (issue) {
        ServiceBindingIssue.Timeout -> SentryLog.e(TAG, "Timing out IPC connection to $targetPackageName")
        is ServiceBindingIssue.NotFoundOrNoPermission -> SentryLog.i(TAG, "Didn't find expected service in $targetPackageName")
        ServiceBindingIssue.BindingDied -> SentryLog.i(TAG, "$targetPackageName died during service binding activity")
        ServiceBindingIssue.NullBinding -> SentryLog.wtf(TAG, "Got null binding from $targetPackageName")
    }
}
