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
import com.infomaniak.core.auth.extensions.getInfomaniakLogin
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.cancellable
import com.infomaniak.core.network.networking.DefaultHttpClientProvider
import com.infomaniak.core.sentry.SentryLog

object LogoutUtils {

    private val TAG = LogoutUtils::class.java.simpleName

    suspend fun logoutToken(appContext: Context, user: User, applicationId: String, clientId: String) {
        runCatching {
            appContext.getInfomaniakLogin(applicationId, clientId).deleteToken(
                okHttpClient = DefaultHttpClientProvider.okHttpClient,
                token = user.apiToken,
            )?.let { errorStatus ->
                SentryLog.i(TAG, "API response error: $errorStatus")
            }
        }.cancellable().onFailure { exception ->
            SentryLog.e(TAG, "Failure on logoutToken ", exception)
        }
    }
}
