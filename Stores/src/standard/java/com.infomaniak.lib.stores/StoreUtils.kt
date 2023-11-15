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

    private const val UPDATE_TYPE = AppUpdateType.FLEXIBLE

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var onInstallDownloaded: () -> Unit

    // Create a listener to track request state updates.
    private val installStateUpdatedListener by lazy {
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> onInstallDownloaded()
                InstallStatus.INSTALLED -> unregisterAppUpdateListener()
                else -> Unit
            }
        }
    }

    //region In-App Update

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
                && appUpdateInfo.isUpdateTypeAllowed(UPDATE_TYPE)
            ) {
                startUpdateFlow(appUpdateInfo, resultLauncher)
            }
        }
    }

    fun checkStalledUpdate() = with(appUpdateManager) {
        registerListener(installStateUpdatedListener)
        appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // If the update is downloaded but not installed, notify the user to complete the update.
                onInstallDownloaded.invoke()
            }
        }
    }

    fun installDownloadedUpdate(onFailure: (Exception) -> Unit) {
        appUpdateManager.completeUpdate().addOnFailureListener(onFailure)
    }

    fun unregisterAppUpdateListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private fun startUpdateFlow(
        appUpdateInfo: AppUpdateInfo,
        downloadUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    ) = with(appUpdateManager) {
        registerListener(installStateUpdatedListener)
        startUpdateFlowForResult(
            appUpdateInfo,
            downloadUpdateResultLauncher,
            AppUpdateOptions.newBuilder(UPDATE_TYPE).build(),
        )
    }
    //endregion

    //region In-App Review

    fun FragmentActivity.launchInAppReview() {
        ReviewManagerFactory.create(this).apply {
            val requestReviewFlow = requestReviewFlow()
            requestReviewFlow.addOnCompleteListener { request ->
                if (request.isSuccessful) launchReviewFlow(this@launchInAppReview, request.result)
            }
        }
    }
    //endregion
}
