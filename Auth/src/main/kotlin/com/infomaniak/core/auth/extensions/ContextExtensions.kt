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

package com.infomaniak.core.auth.extensions

import android.content.Context
import com.infomaniak.core.network.LOGIN_ENDPOINT_URL
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.lib.login.InfomaniakLogin

fun Context.getInfomaniakLogin(appUID: String, clientID: String) = InfomaniakLogin(
    context = this,
    loginUrl = "${LOGIN_ENDPOINT_URL}/",
    appUID = appUID,
    clientID = clientID,
    accessType = null,
    sentryCallback = { error -> SentryLog.e(tag = "WebViewLogin", error) },
)
