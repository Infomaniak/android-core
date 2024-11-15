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

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installErrorCode
import com.infomaniak.lib.core.utils.SentryLog
import com.infomaniak.lib.stores.BaseInAppUpdateManager
import com.infomaniak.lib.stores.StoreUtils
import com.infomaniak.lib.stores.StoresSettingsRepository
import io.sentry.Sentry

/**
 * Manager encapsulating all the needed logic for Google Play's in-app update api
 * Implements [BaseInAppUpdateManager] to add compatibility with fDroid
 *
 * @param activity: Activity on which the lifecycleObserver will be bound
 * @param appId: Parameter needed for the fDroid manager, won't compile without it
 * @param versionCode: Parameter needed for the fDroid manager, won't compile without it
 */
class InAppUpdateManager(
    private val activity: FragmentActivity,
    appId: String,
    versionCode: Int,
) : BaseInAppUpdateManager(activity) {

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    // Result of in app update's bottomSheet user choice
    private val inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest> = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.isUpdateBottomSheetShown = false
        val isUserWantingUpdate = result.resultCode == AppCompatActivity.RESULT_OK
        viewModel.set(StoresSettingsRepository.IS_USER_WANTING_UPDATES_KEY, isUserWantingUpdate)
        onUserChoice?.invoke(isUserWantingUpdate)
    }

    private val onUpdateDownloaded = { viewModel.set(StoresSettingsRepository.HAS_APP_UPDATE_DOWNLOADED_KEY, true) }
    private val onUpdateInstalled = { viewModel.set(StoresSettingsRepository.HAS_APP_UPDATE_DOWNLOADED_KEY, false) }

    private var onUserChoice: ((Boolean) -> Unit)? = null
    private var onInstallStart: (() -> Unit)? = null
    private var onInstallFailure: ((Exception) -> Unit)? = null
    private var onInstallSuccess: (() -> Unit)? = null

    private var updateType: Int = StoreUtils.DEFAULT_UPDATE_TYPE

    // Create a listener to track request state updates.
    private val installStateUpdatedListener by lazy {
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    SentryLog.d(StoreUtils.APP_UPDATE_TAG, "OnUpdateDownloaded triggered by InstallStateUpdated listener")
                    if (updateType == AppUpdateType.FLEXIBLE) onUpdateDownloaded()
                }
                InstallStatus.FAILED -> {
                    SentryLog.d(StoreUtils.APP_UPDATE_TAG, "onInstallFailure triggered by InstallStateUpdated listener")
                    if (updateType == AppUpdateType.IMMEDIATE) {
                        viewModel.resetUpdateSettings()
                        onInstallFailure?.invoke(InstallException(state.installErrorCode))
                    }
                }
                InstallStatus.INSTALLED -> {
                    SentryLog.d(StoreUtils.APP_UPDATE_TAG, "OnUpdateInstalled triggered by InstallStateUpdated listener")
                    if (updateType == AppUpdateType.FLEXIBLE) onUpdateInstalled()
                    unregisterAppUpdateListener()
                }
                else -> Unit
            }
        }
    }

    override fun init(
        mustRequireImmediateUpdate: Boolean,
        onUserChoice: ((Boolean) -> Unit)?,
        onInstallStart: (() -> Unit)?,
        onInstallFailure: ((Exception) -> Unit)?,
        onInstallSuccess: (() -> Unit)?,
        onInAppUpdateUiChange: ((Boolean) -> Unit)?,
        onFDroidResult: ((Boolean) -> Unit)?,
    ) {
        this.updateType = if (mustRequireImmediateUpdate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        this.onUserChoice = onUserChoice
        this.onInstallStart = onInstallStart
        this.onInstallFailure = {
            Sentry.captureException(it)
            onInstallFailure?.invoke(it)
        }
        this.onInstallSuccess = onInstallSuccess

        super.init(mustRequireImmediateUpdate, onInAppUpdateUiChange, onFDroidResult)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        observeAppUpdateDownload()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
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
                && appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                SentryLog.d(StoreUtils.APP_UPDATE_TAG, "Update available on GPlay")
                startUpdateFlow(appUpdateInfo)
            }
        }
    }

    override fun installDownloadedUpdate() {
        with(viewModel) {
            set(StoresSettingsRepository.HAS_APP_UPDATE_DOWNLOADED_KEY, false)
            set(StoresSettingsRepository.APP_UPDATE_LAUNCHES_KEY, StoresSettingsRepository.DEFAULT_APP_UPDATE_LAUNCHES)
        }
        onInstallStart?.invoke()
        appUpdateManager.completeUpdate()
            .addOnSuccessListener { onInstallSuccess?.invoke() }
            .addOnFailureListener {
                viewModel.resetUpdateSettings()
                onInstallFailure?.invoke(AppUpdateException(it.message))
            }
    }

    override fun requireUpdate(onFailure: ((Exception) -> Unit)?) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (updateType == AppUpdateType.IMMEDIATE) {
                val isUpdateStalled =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                if (isUpdateStalled || !viewModel.isUpdateBottomSheetShown) startUpdateFlow(appUpdateInfo)
            }
        }.addOnFailureListener {
            Sentry.captureMessage("Impossible to require update") { scope ->
                scope.setTag("reason", it.message.toString())
            }
            it.printStackTrace()
            onFailure?.invoke(it)
        }
    }

    private fun observeAppUpdateDownload() {
        onInAppUpdateUiChange?.let { updateUiCallback ->
            viewModel.canInstallUpdate.observe(activity, updateUiCallback)
        }
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

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) = with(appUpdateManager) {
        registerListener(installStateUpdatedListener)
        viewModel.isUpdateBottomSheetShown = true
        startUpdateFlowForResult(
            appUpdateInfo,
            inAppUpdateResultLauncher,
            AppUpdateOptions.newBuilder(updateType).build(),
        )
    }

    private class AppUpdateException(override val message: String?) : Exception()
}
