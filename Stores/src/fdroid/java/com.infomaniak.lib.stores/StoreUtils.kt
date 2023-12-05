/*
 * Infomaniak Core - Android
 * Copyright (C) 2023 Infomaniak Network SA
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

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.infomaniak.lib.core.fdroidTools.FdroidApiTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object StoreUtils {

    //region legacy App update
    // TODO: Remove this when Ui for kDrive in app update will be made
    fun FragmentActivity.checkUpdateIsAvailable(appId: String, versionCode: Int, onResult: (updateIsAvailable: Boolean) -> Unit) {
        checkUpdateIsAvailable(appId = appId, versionCode = versionCode, inAppResultLauncher = null, onFDroidResult = onResult)
    }
    //endRegion

    //region In-App Update
    fun initAppUpdateManager(context: Context, onUpdateDownloaded: () -> Unit, onUpdateInstalled: () -> Unit) = Unit

    fun FragmentActivity.checkUpdateIsAvailable(
        appId: String,
        versionCode: Int,
        inAppResultLauncher: ActivityResultLauncher<IntentSenderRequest>?,
        onFDroidResult: (updateIsAvailable: Boolean) -> Unit,
    ) {
        lifecycleScope.launch {
            val lastVersionCode = FdroidApiTools().getLastRelease(appId)

            withContext(Dispatchers.Main) { onFDroidResult(versionCode < lastVersionCode) }
        }
    }

    fun checkStalledUpdate() = Unit

    fun installDownloadedUpdate(onFailure: (Exception) -> Unit) = Unit

    fun unregisterAppUpdateListener() = Unit

    fun cancelUpdate() = Unit
    //endregion

    //region In-App Review
    fun FragmentActivity.launchInAppReview() = Unit
    //endRegion
}
