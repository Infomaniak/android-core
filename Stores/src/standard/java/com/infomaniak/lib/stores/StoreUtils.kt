/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2024 Infomaniak Network SA
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

import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.review.ReviewManagerFactory
import com.infomaniak.lib.stores.updaterequired.data.models.AppVersion
import com.infomaniak.lib.stores.updaterequired.data.models.AppVersion.Store

object StoreUtils : StoresUtils {

    const val APP_UPDATE_TAG = "inAppUpdate"
    const val DEFAULT_UPDATE_TYPE = AppUpdateType.FLEXIBLE
    override val REQUIRED_UPDATE_STORE = Store.PLAY_STORE

    //region In-App Review
    override fun FragmentActivity.launchInAppReview() {
        ReviewManagerFactory.create(this).apply {
            requestReviewFlow().addOnCompleteListener { request ->
                if (request.isSuccessful) launchReviewFlow(this@launchInAppReview, request.result)
            }
        }
    }
    //endregion
}
