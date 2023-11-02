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
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory

object StoreUtils {

    const val DAYS_FOR_FLEXIBLE_UPDATE = 3

    private const val UPDATE_TYPE = AppUpdateType.FLEXIBLE

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var onInstallDownloaded: () -> Unit

    // Create a listener to track request state updates.
    private val installStateUpdatedListener by lazy {
        InstallStateUpdatedListener { state ->
            // (Optional) Provide a download progress bar.
            if (state.installStatus() == InstallStatus.DOWNLOADING) {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                // Show update progress bar.
            }

            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            if (state.installStatus() == InstallStatus.DOWNLOADED) onInstallDownloaded()

        }
    }

    fun initAppUpdateManager(context: Context, onInstall: () -> Unit) {
        appUpdateManager = AppUpdateManagerFactory.create(context)
        onInstallDownloaded = onInstall
    }

    fun checkUpdateIsAvailable(
        appId: String,
        versionCode: Int,
        resultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        onResult: (updateIsAvailable: Boolean) -> Unit,
    ) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // && (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= DAYS_FOR_FLEXIBLE_UPDATE
                && appUpdateInfo.isUpdateTypeAllowed(UPDATE_TYPE)
            ) {
                startUpdateFlow(appUpdateInfo, resultLauncher)
            }
        }
    }

    fun checkStalledUpdate(resultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an in-app update is already running, resume the update.
                startUpdateFlow(appUpdateInfo, resultLauncher)
            }

            // If the update is downloaded but not installed,
            // notify the user to complete the update.
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) onInstallDownloaded.invoke()
        }
    }

    private fun startUpdateFlow(
        appUpdateInfo: AppUpdateInfo,
        downloadUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            downloadUpdateResultLauncher,
            AppUpdateOptions.newBuilder(UPDATE_TYPE).build(),
        )
    }

    fun installDownloadedUpdate() = with(appUpdateManager) {
        registerListener(installStateUpdatedListener)
        completeUpdate()
    }

    fun cancelUpdate() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    fun FragmentActivity.launchInAppReview() {
        ReviewManagerFactory.create(this).apply {
            val requestReviewFlow = requestReviewFlow()
            requestReviewFlow.addOnCompleteListener { request ->
                if (request.isSuccessful) launchReviewFlow(this@launchInAppReview, request.result)
            }
        }
    }
}
