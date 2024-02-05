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
package com.infomaniak.lib.stores.updatemanagers

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.infomaniak.lib.stores.StoresLocalSettings

internal class WorkerUpdateManager(appContext: Context) {

    private val appUpdateManager = AppUpdateManagerFactory.create(appContext)
    private val localSettings = StoresLocalSettings.getInstance(appContext)

    fun installDownloadedUpdate(onInstallFailure: (Exception) -> Unit, onInstallSuccess: () -> Unit) {
        localSettings.hasAppUpdateDownloaded = false
        appUpdateManager.completeUpdate()
            .addOnSuccessListener { onInstallSuccess() }
            .addOnFailureListener {
                localSettings.resetUpdateSettings()
                onInstallFailure(it)
            }
    }
}
