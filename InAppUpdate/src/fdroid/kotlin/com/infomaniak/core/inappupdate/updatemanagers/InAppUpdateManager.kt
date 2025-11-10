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
package com.infomaniak.core.inappupdate.updatemanagers

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.appversionchecker.data.models.AppVersion
import com.infomaniak.core.network.NetworkConfiguration.appId
import com.infomaniak.core.network.NetworkConfiguration.appVersionCode
import com.infomaniak.core.inappupdate.FdroidApiTools
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.core.inappupdate.BaseInAppUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InAppUpdateManager(
    private val activity: ComponentActivity,
) : BaseInAppUpdateManager(activity) {

    override val store: AppVersion.Store = AppVersion.Store.PLAY_STORE
    override val appUpdateTag: String = "inAppUpdate"

    override fun checkUpdateIsAvailable() {
        SentryLog.d(appUpdateTag, "Checking for update on FDroid")
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val lastVersionCode = FdroidApiTools().getLastRelease(appId)

            withContext(Dispatchers.Main) { onFDroidResult?.invoke(appVersionCode < lastVersionCode) }
        }
    }
}
