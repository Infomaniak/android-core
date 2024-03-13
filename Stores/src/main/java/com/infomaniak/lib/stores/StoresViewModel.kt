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
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.infomaniak.lib.stores.StoresSettingsRepository.Companion.ALREADY_GAVE_REVIEW_KEY
import com.infomaniak.lib.stores.StoresSettingsRepository.Companion.APP_REVIEW_LAUNCHES_KEY
import com.infomaniak.lib.stores.StoresSettingsRepository.Companion.APP_UPDATE_LAUNCHES_KEY
import com.infomaniak.lib.stores.StoresSettingsRepository.Companion.DEFAULT_APP_UPDATE_LAUNCHES
import com.infomaniak.lib.stores.StoresSettingsRepository.Companion.HAS_APP_UPDATE_DOWNLOADED_KEY
import com.infomaniak.lib.stores.StoresSettingsRepository.Companion.IS_USER_WANTING_UPDATES_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StoresViewModel(application: Application) : AndroidViewModel(application) {

    var isUpdateBottomSheetShown = false

    private val storesSettingsRepository = StoresSettingsRepository(getApplication())

    val canInstallUpdate = storesSettingsRepository
        .flowOf(HAS_APP_UPDATE_DOWNLOADED_KEY)
        .asLiveData(viewModelScope.coroutineContext + Dispatchers.IO)
        .distinctUntilChanged()

    private val numberOfLaunchesForReview = storesSettingsRepository.flowOf(APP_REVIEW_LAUNCHES_KEY)
    private val alreadyGaveReview = storesSettingsRepository.flowOf(ALREADY_GAVE_REVIEW_KEY)
    val canAskReview = alreadyGaveReview.combine(numberOfLaunchesForReview) { alreadyGaveFeedback, numberOfLaunches ->
        !alreadyGaveFeedback && numberOfLaunches < 0
    }
        .asLiveData(viewModelScope.coroutineContext + Dispatchers.IO)
        .distinctUntilChanged()

    fun <T> set(key: Preferences.Key<T>, value: T) = viewModelScope.launch(Dispatchers.IO) {
        storesSettingsRepository.setValue(key, value)
    }

    fun resetUpdateSettings() = viewModelScope.launch(Dispatchers.IO) { storesSettingsRepository.resetUpdateSettings() }

    fun resetReviewSettings() = viewModelScope.launch(Dispatchers.IO) { storesSettingsRepository.resetReviewSettings() }

    //region BaseInAppUpdateManager
    fun decrementAppUpdateLaunches() = viewModelScope.launch(Dispatchers.IO) {
        val appUpdateLaunches = storesSettingsRepository.getValue(APP_UPDATE_LAUNCHES_KEY)
        set(APP_UPDATE_LAUNCHES_KEY, appUpdateLaunches - 1)
    }

    fun shouldCheckUpdate(checkUpdateCallback: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        if (storesSettingsRepository.getValue(IS_USER_WANTING_UPDATES_KEY) ||
            storesSettingsRepository.getValue(APP_UPDATE_LAUNCHES_KEY) <= 0
        ) {
            checkUpdateCallback()
            storesSettingsRepository.setValue(APP_UPDATE_LAUNCHES_KEY, DEFAULT_APP_UPDATE_LAUNCHES)
        }
    }
    //endregion

    //region BaseInAppReviewManager
    fun decrementAppReviewLaunches() = viewModelScope.launch(Dispatchers.IO) {
        val appReviewLaunches = storesSettingsRepository.getValue(APP_REVIEW_LAUNCHES_KEY)
        set(APP_REVIEW_LAUNCHES_KEY, appReviewLaunches - 1)
    }

    fun changeReviewStatus(hasGivenReview: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        set(ALREADY_GAVE_REVIEW_KEY, hasGivenReview)
    }
    //endregion
}
