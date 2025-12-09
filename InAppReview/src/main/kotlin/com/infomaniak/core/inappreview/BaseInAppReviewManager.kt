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
package com.infomaniak.core.inappreview

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import kotlinx.coroutines.flow.emptyFlow

abstract class BaseInAppReviewManager(private val activity: ComponentActivity) : DefaultLifecycleObserver {

    open val shouldDisplayReviewDialog = emptyFlow<Boolean>()
    open fun init(
        countdownBehavior: Behavior = Behavior.LifecycleBased,
        appReviewThreshold: Int? = null,
        maxAppReviewThreshold: Int? = null,
        onUserWantToReview: (() -> Unit)?,
        onUserWantToGiveFeedback: (() -> Unit)?
    ) = Unit

    open fun onUserWantsToReview() = Unit

    open fun onUserWantsToGiveFeedback(feedbackUrl: String) = Unit

    open fun onUserWantsToDismiss() = Unit

    open fun decrementAppReviewCountdown() = Unit

    enum class Behavior {
        /** This behavior uses the activity's lifecycle observer to automatically update the countdown */
        LifecycleBased,
        /** This let the user decides when to update the countdown */
        Manual,
    }
}
