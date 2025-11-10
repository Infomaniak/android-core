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

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installErrorCode
import com.infomaniak.core.appversionchecker.data.models.AppVersion
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository.Companion.APP_UPDATE_LAUNCHES_KEY
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository.Companion.DEFAULT_APP_UPDATE_LAUNCHES
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository.Companion.HAS_APP_UPDATE_DOWNLOADED_KEY
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository.Companion.IS_USER_WANTING_UPDATES_KEY
import com.infomaniak.core.inappupdate.BaseInAppUpdateManager
import com.infomaniak.core.sentry.SentryLog
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manager encapsulating all the needed logic for Google Play's in-app update api
 * Implements [BaseInAppUpdateManager] to add compatibility with fDroid
 *
 * @param activity: Activity on which the lifecycleObserver will be bound
 */
class InAppUpdateManager(
    private val activity: ComponentActivity,
) : BaseInAppUpdateManager(activity) {

    override val store: AppVersion.Store = AppVersion.Store.PLAY_STORE
    override val appUpdateTag: String = APP_UPDATE_TAG

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    // Result of in app update's bottomSheet user choice
    private val inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest> = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        isUpdateBottomSheetShown = false
        val isUserWantingUpdate = result.resultCode == AppCompatActivity.RESULT_OK
        set(IS_USER_WANTING_UPDATES_KEY, isUserWantingUpdate)
        onUserChoice?.invoke(isUserWantingUpdate)
    }

    private val onUpdateDownloaded = { set(HAS_APP_UPDATE_DOWNLOADED_KEY, true) }
    private val onUpdateInstalled = { set(HAS_APP_UPDATE_DOWNLOADED_KEY, false) }

    private var onUserChoice: ((Boolean) -> Unit)? = null
    private var onInstallStart: (() -> Unit)? = null
    private var onInstallFailure: ((Exception) -> Unit)? = null
    private var onInstallSuccess: (() -> Unit)? = null

    private var updateType: Int = DEFAULT_UPDATE_TYPE

    // Create a listener to track request state updates.
    private val installStateUpdatedListener by lazy {
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    SentryLog.d(appUpdateTag, "OnUpdateDownloaded triggered by InstallStateUpdated listener")
                    if (updateType == AppUpdateType.FLEXIBLE) onUpdateDownloaded()
                }
                InstallStatus.FAILED -> {
                    SentryLog.d(appUpdateTag, "onInstallFailure triggered by InstallStateUpdated listener")
                    if (updateType == AppUpdateType.IMMEDIATE) {
                        resetUpdateSettings()
                        onInstallFailure?.invoke(InstallException(state.installErrorCode))
                    }
                }
                InstallStatus.INSTALLED -> {
                    SentryLog.d(appUpdateTag, "OnUpdateInstalled triggered by InstallStateUpdated listener")
                    if (updateType == AppUpdateType.FLEXIBLE) onUpdateInstalled()
                    unregisterAppUpdateListener()
                }
                else -> Unit
            }
        }
    }

    override fun init(
        isUpdateRequired: Boolean,
        onUserChoice: ((Boolean) -> Unit)?,
        onInstallStart: (() -> Unit)?,
        onInstallFailure: ((Exception) -> Unit)?,
        onInstallSuccess: (() -> Unit)?,
        onInAppUpdateUiChange: ((Boolean) -> Unit)?,
        onFDroidResult: ((Boolean) -> Unit)?,
    ) {
        this.updateType = if (isUpdateRequired) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        this.onUserChoice = onUserChoice
        this.onInstallStart = onInstallStart
        this.onInstallFailure = {
            Sentry.captureException(it)
            onInstallFailure?.invoke(it)
        }
        this.onInstallSuccess = onInstallSuccess

        super.init(isUpdateRequired, onInAppUpdateUiChange, onFDroidResult)
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
        SentryLog.d(appUpdateTag, "Checking for update on GPlay")
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            SentryLog.d(appUpdateTag, "checking success")
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                SentryLog.d(appUpdateTag, "Update available on GPlay")
                startUpdateFlow(appUpdateInfo)
            }
        }
    }

    override fun installDownloadedUpdate() {
        set(HAS_APP_UPDATE_DOWNLOADED_KEY, false)
        set(APP_UPDATE_LAUNCHES_KEY, DEFAULT_APP_UPDATE_LAUNCHES)

        onInstallStart?.invoke()
        appUpdateManager.completeUpdate()
            .addOnSuccessListener { onInstallSuccess?.invoke() }
            .addOnFailureListener {
                resetUpdateSettings()
                onInstallFailure?.invoke(AppUpdateException(it.message))
            }
    }

    override fun requireUpdate(onFailure: ((Exception) -> Unit)?) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (updateType == AppUpdateType.IMMEDIATE) {
                val isUpdateStalled =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                if (isUpdateStalled || !isUpdateBottomSheetShown) startUpdateFlow(appUpdateInfo)
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
            activity.lifecycleScope.launch(Dispatchers.IO) {
                canInstallUpdate.collect(updateUiCallback)
            }
        }
    }

    private fun checkStalledUpdate(): Unit = with(appUpdateManager) {
        registerListener(installStateUpdatedListener)
        appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                SentryLog.d(appUpdateTag, "CheckStalledUpdate downloaded")
                // If the update is downloaded but not installed, notify the user to complete the update.
                onUpdateDownloaded.invoke()
            }
        }
    }

    private fun unregisterAppUpdateListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) = with(appUpdateManager) {
        runCatching {
            registerListener(installStateUpdatedListener)
            isUpdateBottomSheetShown = true
            startUpdateFlowForResult(
                appUpdateInfo,
                inAppUpdateResultLauncher,
                AppUpdateOptions.newBuilder(updateType).build(),
            )
        }.onFailure { exception ->
            exception.printStackTrace()
            exception.message?.let {
                Sentry.captureMessage(it, SentryLevel.WARNING) { scope ->
                    scope.setExtra("contract", inAppUpdateResultLauncher.contract.toString())
                    scope.setTag("updateType", updateType.toString())
                    scope.setTag("onInstallFailure is null", (onInstallFailure == null).toString())
                }
            }
        }
    }

    private class AppUpdateException(override val message: String?) : Exception()

    companion object {
        const val APP_UPDATE_TAG = "inAppUpdate"
        const val DEFAULT_UPDATE_TYPE = AppUpdateType.FLEXIBLE
    }
}
