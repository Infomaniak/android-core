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
import com.google.android.play.core.review.ReviewManagerFactory
import com.infomaniak.core.inappreview.AppReviewSettingsRepository
import com.infomaniak.core.inappreview.AppReviewSettingsRepository.Companion.ALREADY_GAVE_REVIEW_KEY
import com.infomaniak.core.inappreview.AppReviewSettingsRepository.Companion.APP_REVIEW_THRESHOLD_KEY
import com.infomaniak.core.inappreview.BaseInAppReviewManager
import com.infomaniak.core.webview.ui.WebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class InAppReviewManager(private val activity: ComponentActivity) : BaseInAppReviewManager(activity) {

    private val appReviewSettingsRepository = AppReviewSettingsRepository(activity)

    private val appReviewCountdown = appReviewSettingsRepository.flowOf(APP_REVIEW_THRESHOLD_KEY)
    private val alreadyGaveReview = appReviewSettingsRepository.flowOf(ALREADY_GAVE_REVIEW_KEY)

    val shouldDisplayReviewDialog =
        alreadyGaveReview.combine(appReviewCountdown) { alreadyGaveReview, countdown ->
            !alreadyGaveReview && countdown <= 0
        }.distinctUntilChanged()

    override fun init(countdownBehavior: Behavior, appReviewThreshold: Int?, maxAppReviewThreshold: Int?) {
        if (countdownBehavior == Behavior.LifecycleBased) activity.lifecycle.addObserver(observer = this)
        appReviewThreshold?.let { appReviewSettingsRepository.appReviewThreshold = it }
        maxAppReviewThreshold?.let { appReviewSettingsRepository.maxAppReviewThreshold = it }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        decrementAppReviewCountdown()
    }

    //region public interface
    override fun onUserWantsToReview() {
        resetAppReviewSettings()
        setAppReviewedStatus()
        launchInAppReview()
    }

    override fun onUserWantsToGiveFeedback(feedbackUrl: String) {
        resetAppReviewSettings()
        WebViewActivity.startActivity(activity, feedbackUrl)
    }

    override fun onUserWantsToDismiss() {
        resetAppReviewSettings()
    }

    override fun decrementAppReviewCountdown() {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val appReviewCountdown = appReviewSettingsRepository.getValue(APP_REVIEW_THRESHOLD_KEY)
            alreadyGaveReview.collectLatest { hasGivenReview ->
                if (!hasGivenReview) set(APP_REVIEW_THRESHOLD_KEY, appReviewCountdown - 1)
            }
        }
    }
    //endregion

    private fun resetAppReviewSettings() = activity.lifecycleScope.launch(Dispatchers.IO) {
        appReviewSettingsRepository.resetReviewSettings()
    }

    private fun setAppReviewedStatus() = activity.lifecycleScope.launch(Dispatchers.IO) {
        set(ALREADY_GAVE_REVIEW_KEY, true)
    }

    private fun <T> set(key: Preferences.Key<T>, value: T) = activity.lifecycleScope.launch(Dispatchers.IO) {
        appReviewSettingsRepository.setValue(key, value)
    }

    private fun launchInAppReview() {
        ReviewManagerFactory.create(activity).apply {
            requestReviewFlow().addOnCompleteListener { request ->
                if (request.isSuccessful) launchReviewFlow(activity, request.result)
            }
        }
    }
}
