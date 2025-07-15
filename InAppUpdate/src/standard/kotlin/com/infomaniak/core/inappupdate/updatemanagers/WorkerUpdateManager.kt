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

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class WorkerUpdateManager(appContext: Context) {

    private val appUpdateManager = AppUpdateManagerFactory.create(appContext)
    private val storesSettingsRepository = AppUpdateSettingsRepository(appContext)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun installDownloadedUpdate(onInstallFailure: (Exception) -> Unit, onInstallSuccess: () -> Unit) {
        coroutineScope.launch {
            storesSettingsRepository.setValue(AppUpdateSettingsRepository.HAS_APP_UPDATE_DOWNLOADED_KEY, false)
        }
        appUpdateManager.completeUpdate()
            .addOnSuccessListener { onInstallSuccess() }
            .addOnFailureListener {
                coroutineScope.launch { storesSettingsRepository.resetUpdateSettings() }
                onInstallFailure(it)
            }
    }
}
