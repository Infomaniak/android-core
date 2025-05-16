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
package com.infomaniak.core.inappreview.reviewmanagers

import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.inappreview.AppReviewSettingsRepository
import com.infomaniak.core.inappreview.AppReviewSettingsRepository.Companion.ALREADY_GAVE_REVIEW_KEY
import com.infomaniak.core.inappreview.AppReviewSettingsRepository.Companion.APP_REVIEW_THRESHOLD_KEY
import com.infomaniak.core.inappreview.BaseInAppReviewManager
import com.infomaniak.core.inappreview.StoreUtils.launchInAppReview
import com.infomaniak.core.webview.ui.WebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class InAppReviewManager(private val activity: ComponentActivity) : BaseInAppReviewManager(activity) {

    private val appReviewSettingsRepository = AppReviewSettingsRepository(activity)

    private val appReviewCounter = appReviewSettingsRepository.flowOf(APP_REVIEW_THRESHOLD_KEY)
    private val alreadyGaveReview = appReviewSettingsRepository.flowOf(ALREADY_GAVE_REVIEW_KEY)

    val shouldDisplayReviewDialog =
        alreadyGaveReview.combine(appReviewCounter) { alreadyGaveFeedback, numberOfLaunches ->
            !alreadyGaveFeedback && numberOfLaunches < 0
        }.distinctUntilChanged()

    override fun init() {
        activity.lifecycle.addObserver(observer = this)
        super.init()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        decrementAppReviewLaunches()
    }

    fun onUserWantsToReview() {
        changeReviewStatus(true)
        activity.launchInAppReview()
    }

    fun onUserWantsToGiveFeedback(feedbackUrl: String) {
        changeReviewStatus(true)
        WebViewActivity.startActivity(activity, feedbackUrl)
    }

    fun onUserWantsToDismiss() {
        resetReviewSettings()
    }

    //region AppReviewSettings
    fun <T> set(key: Preferences.Key<T>, value: T) = activity.lifecycleScope.launch(Dispatchers.IO) {
        appReviewSettingsRepository.setValue(key, value)
    }

    fun resetReviewSettings() = activity.lifecycleScope.launch(Dispatchers.IO) {
        appReviewSettingsRepository.resetReviewSettings()
    }

    fun decrementAppReviewLaunches() = activity.lifecycleScope.launch(Dispatchers.IO) {
        val appReviewLaunches = appReviewSettingsRepository.getValue(APP_REVIEW_THRESHOLD_KEY)
        set(APP_REVIEW_THRESHOLD_KEY, appReviewLaunches - 1)
    }

    fun changeReviewStatus(hasGivenReview: Boolean) = activity.lifecycleScope.launch(Dispatchers.IO) {
        set(ALREADY_GAVE_REVIEW_KEY, hasGivenReview)
    }
    //endregion
}
