/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.lib.stores

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.infomaniak.lib.core.fdroidTools.FdroidApiTools
import com.infomaniak.lib.core.utils.SentryLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InAppUpdateManager(
    private val activity: FragmentActivity,
    private val appId: String,
    private val versionCode: Int,
    onUserChoice: (Boolean) -> Unit,
    onInstallStart: () -> Unit,
    onInstallFailure: (Exception) -> Unit,
    onInstallSuccess: (() -> Unit)? = null,
) : BaseInAppUpdateManager(activity) {

    override fun installDownloadedUpdate() = Unit

    override fun checkUpdateIsAvailable() {
        SentryLog.d(StoreUtils.APP_UPDATE_TAG, "Checking for update on FDroid")
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val lastVersionCode = FdroidApiTools().getLastRelease(appId)

            withContext(Dispatchers.Main) { onFDroidResult?.invoke(versionCode < lastVersionCode) }
        }
    }
}
