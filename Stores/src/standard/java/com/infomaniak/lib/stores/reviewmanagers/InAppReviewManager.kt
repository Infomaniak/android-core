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
package com.infomaniak.lib.stores.reviewmanagers

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.infomaniak.lib.core.utils.UtilsUi.openUrl
import com.infomaniak.lib.stores.BaseInAppReviewManager
import com.infomaniak.lib.stores.StoreUtils.launchInAppReview
import com.infomaniak.lib.stores.ui.dialogs.ReviewAlertDialog
import com.infomaniak.lib.stores.ui.dialogs.ReviewAlertDialogData
import com.infomaniak.lib.core.R as RCore

class InAppReviewManager(
    private val activity: FragmentActivity,
    private val reviewDialogTheme: Int,
    private val reviewDialogTitleResId: Int,
    private val feedbackUrlResId: Int,
) : BaseInAppReviewManager(activity) {

    private var onDialogShown: (() -> Unit)? = null
    private var onUserWantsToReview: (() -> Unit)? = null
    private var onUserWantsToGiveFeedback: (() -> Unit)? = null

    override fun init(
        onDialogShown: (() -> Unit)?,
        onUserWantToReview: (() -> Unit)?,
        onUserWantToGiveFeedback: (() -> Unit)?,
    ) {
        this.onDialogShown = onDialogShown
        this.onUserWantsToReview = onUserWantToReview
        this.onUserWantsToGiveFeedback = onUserWantToGiveFeedback

        activity.lifecycle.addObserver(observer = this)

        super.init(onDialogShown, onUserWantToReview, onUserWantToGiveFeedback)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        observeInAppReview()
    }

     private fun onUserWantToReview() {
        onUserWantsToReview?.invoke()
        viewModel.changeReviewStatus(true)
        activity.launchInAppReview()
    }

    private fun observeInAppReview() {
        viewModel.canAskReview.observe(activity) { canShowReviewDialog ->
            if (canShowReviewDialog) {
                viewModel.resetReviewSettings()
                showAppReviewDialog()
            }
        }
    }

    private fun showAppReviewDialog() = with(activity) {
        onDialogShown?.invoke()
        ReviewAlertDialog(
            activityContext = this,
            customThemeRes = reviewDialogTheme,
            reviewAlertDialogData = ReviewAlertDialogData(
                title = getString(reviewDialogTitleResId),
                positiveText = getString(RCore.string.buttonYes),
                negativeText = getString(RCore.string.buttonNo),
                onPositiveButtonClicked = ::onUserWantToReview,
                onNegativeButtonClicked = {
                    onUserWantsToGiveFeedback?.invoke()
                    openUrl(getString(feedbackUrlResId))
                },
            ),
        ).show()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        viewModel.decrementAppReviewLaunches()
    }
}
