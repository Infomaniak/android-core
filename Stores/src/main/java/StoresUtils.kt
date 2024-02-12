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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface StoresUtils {

    //region In-App Updates
    fun FragmentActivity.initAppUpdateManager(
        onUserChoice: (Boolean) -> Unit,
        onUpdateDownloaded: () -> Unit,
        onUpdateInstalled: () -> Unit,
    ) = Unit

    fun FragmentActivity.checkUpdateIsAvailable(
        appId: String,
        versionCode: Int,
        onFDroidResult: (updateIsAvailable: Boolean) -> Unit,
    ) {
        lifecycleScope.launch {
            val lastVersionCode = FdroidApiTools().getLastRelease(appId)

            withContext(Dispatchers.Main) { onFDroidResult(versionCode < lastVersionCode) }
        }
    }

    fun checkStalledUpdate() = Unit

    fun installDownloadedUpdate(
        onInstallStart: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) = Unit

    fun unregisterAppUpdateListener() = Unit
    //endregion

    //region In-App Review
    fun FragmentActivity.launchInAppReview() = Unit
    //endRegion
}
