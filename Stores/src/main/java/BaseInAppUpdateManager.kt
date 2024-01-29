/*
 * Infomaniak kDrive - Android
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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.infomaniak.lib.core.fdroidTools.FdroidApiTools
import com.infomaniak.lib.core.utils.SentryLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseInAppUpdateManager(
    private val activity: FragmentActivity,
    private val appId: String,
    private val versionCode: Int,
    private val onFDroidResult: (Boolean) -> Unit,
    private val onUserChoice: ((Boolean) -> Unit)? = null,
    private val onInstallStart: (() -> Unit)? = null,
    private val onInstallFailure: ((Exception) -> Unit)? = null,
    private val onInstallSuccess: (() -> Unit)? = null,
) : DefaultLifecycleObserver {

    var onInAppUpdateUiChange: ((Boolean) -> Unit)? = null

    protected val localSettings = StoresLocalSettings.getInstance(activity)

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        localSettings.appUpdateLaunches++
        handleUpdates()
    }

    open fun installDownloadedUpdate() = Unit

    protected open fun checkUpdateIsAvailable() {
        SentryLog.d(StoreUtils.APP_UPDATE_TAG, "Checking for update on FDroid")
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val lastVersionCode = FdroidApiTools().getLastRelease(appId)

            withContext(Dispatchers.Main) { onFDroidResult(versionCode < lastVersionCode) }
        }
    }

    private fun handleUpdates() = with(localSettings) {
        if (isUserWantingUpdates || (appUpdateLaunches != 0 && appUpdateLaunches % 10 == 0)) checkUpdateIsAvailable()
    }
}
