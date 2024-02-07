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

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoresViewModel(application: Application) : AndroidViewModel(application) {

    private inline val context: Context get() = getApplication()
    private val ioCoroutineContext = viewModelScope.coroutineContext + Dispatchers.IO
    private val storesSettingsRepository = StoresSettingsRepository(context)

    val storesSettingsLiveData = storesSettingsRepository.flow.asLiveData(ioCoroutineContext)

    fun <T> liveDataOf(key: Preferences.Key<T>): LiveData<T> {
        return storesSettingsRepository.flowOf(key).asLiveData(ioCoroutineContext).distinctUntilChanged()
    }

    fun <T> set(key: Preferences.Key<T>, value: T) = viewModelScope.launch(ioCoroutineContext) {
        storesSettingsRepository.setValue(key, value)
    }

    fun resetUpdateSettings() = viewModelScope.launch(ioCoroutineContext) { storesSettingsRepository.resetUpdateSettings() }

    //region BaseInAppUpdateManager
    fun incrementAppUpdateLaunches() = viewModelScope.launch(ioCoroutineContext) {
        val appUpdateLaunches = storesSettingsRepository.getValue(StoresSettingsRepository.APP_UPDATE_LAUNCHES)
        set(StoresSettingsRepository.APP_UPDATE_LAUNCHES, appUpdateLaunches + 1)
    }

    fun shouldCheckUpdate(checkUpdateCallback: () -> Unit) = viewModelScope.launch(ioCoroutineContext) {
        with(storesSettingsRepository.getAll()) {
            if (appUpdateLaunches != 0 && (isUserWantingUpdates || appUpdateLaunches % 10 == 0)) checkUpdateCallback()
        }
    }
    //endregion
}
