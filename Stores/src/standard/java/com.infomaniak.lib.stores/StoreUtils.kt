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

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.infomaniak.lib.core.utils.SentryLog

object StoreUtils : StoresUtils {

    override val APP_UPDATE_TAG = "inAppUpdate"

    private const val UPDATE_TYPE = AppUpdateType.FLEXIBLE

    private lateinit var appUpdateManager: AppUpdateManager
    // Result of in app update's bottomSheet user choice
    private lateinit var inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var localSettings: StoresLocalSettings
    private lateinit var onUpdateDownloaded: () -> Unit
    private lateinit var onUpdateInstalled: () -> Unit

    // Create a listener to track request state updates.
    private val installStateUpdatedListener by lazy {
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    SentryLog.d(APP_UPDATE_TAG, "OnUpdateDownloaded triggered by InstallStateUpdated listener")
                    onUpdateDownloaded()
                }
                InstallStatus.INSTALLED -> {
                    SentryLog.d(APP_UPDATE_TAG, "OnUpdateInstalled triggered by InstallStateUpdated listener")
                    onUpdateInstalled()
                    unregisterAppUpdateListener()
                }
                else -> Unit
            }
        }
    }

    //region In-App Update
    override fun FragmentActivity.initAppUpdateManager(
        onUserChoice: (Boolean) -> Unit,
        onUpdateDownloaded: () -> Unit,
        onUpdateInstalled: () -> Unit,
    ) {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        localSettings = StoresLocalSettings.getInstance(context = this)
        this@StoreUtils.onUpdateDownloaded = onUpdateDownloaded
        this@StoreUtils.onUpdateInstalled = onUpdateInstalled

        inAppUpdateResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            val isUserWantingUpdate = result.resultCode == AppCompatActivity.RESULT_OK
            localSettings.isUserWantingUpdates = isUserWantingUpdate
            onUserChoice(isUserWantingUpdate)
        }
    }

    override fun FragmentActivity.checkUpdateIsAvailable(
        appId: String,
        versionCode: Int,
        onFDroidResult: (updateIsAvailable: Boolean) -> Unit,
    ) {
        SentryLog.d(APP_UPDATE_TAG, "Checking for update on GPlay")
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(UPDATE_TYPE)
            ) {
                SentryLog.d(APP_UPDATE_TAG, "Update available on GPlay")
                startUpdateFlow(appUpdateInfo, inAppUpdateResultLauncher)
            }
        }
    }

    override fun checkStalledUpdate(): Unit = with(appUpdateManager) {
        registerListener(installStateUpdatedListener)
        appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                SentryLog.d(APP_UPDATE_TAG, "CheckStalledUpdate downloaded")
                // If the update is downloaded but not installed, notify the user to complete the update.
                onUpdateDownloaded.invoke()
            }
        }
    }

    override fun installDownloadedUpdate(
        onInstallStart: (() -> Unit)?,
        onSuccess: (() -> Unit)?,
        onFailure: ((Exception) -> Unit)?,
    ) {
        localSettings.hasAppUpdateDownloaded = false
        appUpdateManager.completeUpdate()
            .addOnSuccessListener { onSuccess?.invoke() }
            .addOnFailureListener {
                localSettings.resetUpdateSettings()
                onFailure?.invoke(it)
            }
    }

    override fun unregisterAppUpdateListener() {
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
    override fun FragmentActivity.launchInAppReview() {
        ReviewManagerFactory.create(this).apply {
            requestReviewFlow().addOnCompleteListener { request ->
                if (request.isSuccessful) launchReviewFlow(this@launchInAppReview, request.result)
            }
        }
    }
    //endregion
}
