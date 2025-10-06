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
package com.infomaniak.core.inappupdate

import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.extensions.goToAppStore
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository.Companion.APP_UPDATE_LAUNCHES_KEY
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository.Companion.DEFAULT_APP_UPDATE_LAUNCHES
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository.Companion.HAS_APP_UPDATE_DOWNLOADED_KEY
import com.infomaniak.core.inappupdate.AppUpdateSettingsRepository.Companion.IS_USER_WANTING_UPDATES_KEY
import com.infomaniak.core.inappupdate.updaterequired.data.api.ApiRepositoryStores
import com.infomaniak.core.network.NetworkConfiguration.appId
import com.infomaniak.core.network.NetworkConfiguration.appVersionName
import com.infomaniak.core.network.networking.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseInAppUpdateManager(private val activity: ComponentActivity) : DefaultLifecycleObserver {

    protected var onInAppUpdateUiChange: ((Boolean) -> Unit)? = null
    protected var onFDroidResult: ((Boolean) -> Unit)? = null

    private val storesSettingsRepository = AppUpdateSettingsRepository(activity)

    var isUpdateBottomSheetShown = false

    val canInstallUpdate = storesSettingsRepository
        .flowOf(HAS_APP_UPDATE_DOWNLOADED_KEY).distinctUntilChanged()

    val isUpdateRequired = flow {
        val apiResponse = ApiRepositoryStores.getAppVersion(appId, HttpClient.okHttpClient)

        emit(apiResponse.data?.mustRequireUpdate(appVersionName) == true)
    }.stateIn(
        scope = activity.lifecycleScope,
        started = SharingStarted.Eagerly,
        initialValue = false,
    )

    open fun installDownloadedUpdate() = Unit

    // TODO: What about the F-Droid variant?
    open fun requireUpdate(onFailure: ((Exception) -> Unit)? = null) = activity.goToAppStore()

    open fun init(
        isUpdateRequired: Boolean = false,
        onUserChoice: ((Boolean) -> Unit)? = null,
        onInstallStart: (() -> Unit)? = null,
        onInstallFailure: ((Exception) -> Unit)? = null,
        onInstallSuccess: (() -> Unit)? = null,
        onInAppUpdateUiChange: ((Boolean) -> Unit)? = null,
        onFDroidResult: ((Boolean) -> Unit)? = null,
    ) = init(isUpdateRequired, onInAppUpdateUiChange, onFDroidResult)

    protected fun init(
        isUpdateRequired: Boolean = false,
        onInAppUpdateUiChange: ((Boolean) -> Unit)? = null,
        onFDroidResult: ((Boolean) -> Unit)? = null,
    ) {
        this.onInAppUpdateUiChange = onInAppUpdateUiChange
        this.onFDroidResult = onFDroidResult

        if (!isUpdateRequired) activity.lifecycle.addObserver(observer = this)
    }

    protected abstract fun checkUpdateIsAvailable()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        if (!isUpdateBottomSheetShown) shouldCheckUpdate(::checkUpdateIsAvailable)
        decrementAppUpdateLaunches()
    }

    //region Repository
    fun <T> set(key: Preferences.Key<T>, value: T) = activity.lifecycleScope.launch(Dispatchers.IO) {
        storesSettingsRepository.setValue(key, value)
    }

    fun resetUpdateSettings() = activity.lifecycleScope.launch(Dispatchers.IO) { storesSettingsRepository.resetUpdateSettings() }

    fun decrementAppUpdateLaunches() = activity.lifecycleScope.launch(Dispatchers.IO) {
        val appUpdateLaunches = storesSettingsRepository.getValue(APP_UPDATE_LAUNCHES_KEY)
        set(APP_UPDATE_LAUNCHES_KEY, appUpdateLaunches - 1)
    }

    fun shouldCheckUpdate(checkUpdateCallback: () -> Unit) = activity.lifecycleScope.launch(Dispatchers.IO) {
        if (storesSettingsRepository.getValue(IS_USER_WANTING_UPDATES_KEY) ||
            storesSettingsRepository.getValue(APP_UPDATE_LAUNCHES_KEY) <= 0
        ) {
            checkUpdateCallback()
            storesSettingsRepository.setValue(APP_UPDATE_LAUNCHES_KEY, DEFAULT_APP_UPDATE_LAUNCHES)
        }
    }
    //endregion
}
