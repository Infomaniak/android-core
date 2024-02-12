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

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.infomaniak.lib.core.utils.SentryLog

class InAppUpdateManager(
    private val activity: FragmentActivity,
    appId: String,
    versionCode: Int,
    private val onUserChoice: (Boolean) -> Unit,
    private val onInstallStart: () -> Unit,
    private val onInstallFailure: (Exception) -> Unit,
    private val onInstallSuccess: (() -> Unit)? = null,
) : BaseInAppUpdateManager(activity) {

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    // Result of in app update's bottomSheet user choice
    private val inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest> = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val isUserWantingUpdate = result.resultCode == AppCompatActivity.RESULT_OK
        localSettings.isUserWantingUpdates = isUserWantingUpdate
        onUserChoice(isUserWantingUpdate)
    }

    private val viewModel: StoresViewModel by lazy { ViewModelProvider(activity)[StoresViewModel::class.java] }

    private val onUpdateDownloaded = { viewModel.toggleAppUpdateStatus(localSettings, isUpdateDownloaded = true) }
    private val onUpdateInstalled = { viewModel.toggleAppUpdateStatus(localSettings, isUpdateDownloaded = false) }

    // Create a listener to track request state updates.
    private val installStateUpdatedListener by lazy {
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    SentryLog.d(StoreUtils.APP_UPDATE_TAG, "OnUpdateDownloaded triggered by InstallStateUpdated listener")
                    onUpdateDownloaded()
                }
                InstallStatus.INSTALLED -> {
                    SentryLog.d(StoreUtils.APP_UPDATE_TAG, "OnUpdateInstalled triggered by InstallStateUpdated listener")
                    onUpdateInstalled()
                    unregisterAppUpdateListener()
                }
                else -> Unit
            }
        }
    }

    init {
        observeLifecycle()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        observeAppUpdateDownload()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        viewModel.canInstallUpdate.value = localSettings.hasAppUpdateDownloaded
        checkStalledUpdate()
    }

    override fun onStop(owner: LifecycleOwner) {
        unregisterAppUpdateListener()
        super.onStop(owner)
    }

    override fun checkUpdateIsAvailable() {
        SentryLog.d(StoreUtils.APP_UPDATE_TAG, "Checking for update on GPlay")
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            SentryLog.d(StoreUtils.APP_UPDATE_TAG, "checking success")
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(StoreUtils.UPDATE_TYPE)
            ) {
                SentryLog.d(StoreUtils.APP_UPDATE_TAG, "Update available on GPlay")
                startUpdateFlow(appUpdateInfo, inAppUpdateResultLauncher)
            }
        }
    }

    override fun installDownloadedUpdate() {
        localSettings.hasAppUpdateDownloaded = false
        viewModel.canInstallUpdate.value = false
        onInstallStart()
        appUpdateManager.completeUpdate()
            .addOnSuccessListener { onInstallSuccess?.invoke() }
            .addOnFailureListener {
                localSettings.resetUpdateSettings()
                onInstallFailure(it)
            }
    }

    private fun observeAppUpdateDownload() {
        viewModel.canInstallUpdate.observe(activity) { isUpdateDownloaded -> onInAppUpdateUiChange?.invoke(isUpdateDownloaded) }
    }

    private fun checkStalledUpdate(): Unit = with(appUpdateManager) {
        registerListener(installStateUpdatedListener)
        appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                SentryLog.d(StoreUtils.APP_UPDATE_TAG, "CheckStalledUpdate downloaded")
                // If the update is downloaded but not installed, notify the user to complete the update.
                onUpdateDownloaded.invoke()
            }
        }
    }

    private fun unregisterAppUpdateListener() {
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
            AppUpdateOptions.newBuilder(StoreUtils.UPDATE_TYPE).build(),
        )
    }
}
