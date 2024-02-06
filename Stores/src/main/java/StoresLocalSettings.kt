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

import android.content.Context
import com.infomaniak.lib.core.utils.SharedValues
import com.infomaniak.lib.core.utils.transaction

class StoresLocalSettings private constructor(context: Context) : SharedValues {

    override val sharedPreferences = context.applicationContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)!!

    var isUserWantingUpdates by sharedValue("isUserWantingUpdatesKey", true)
    var hasAppUpdateDownloaded by sharedValue("hasAppUpdateDownloadedKey", false)
    var appUpdateLaunches by sharedValue("appUpdateLaunchesKey", 0)
    var appReviewLaunches by sharedValue("appReviewLaunchesKey", DEFAULT_APP_REVIEW_LAUNCHES)
    var showAppReviewDialog by sharedValue("showAppReviewDialogKey", true)

    fun removeSettings() = sharedPreferences.transaction { clear() }

    fun resetUpdateSettings() {
        isUserWantingUpdates = false // This avoid the user being instantly reprompted to download update
        hasAppUpdateDownloaded = false
    }

    companion object {

        const val DEFAULT_APP_REVIEW_LAUNCHES = 50

        private const val SHARED_PREFS_NAME = "StoresLocalSettingsSharedPref"

        @Volatile
        private var INSTANCE: StoresLocalSettings? = null

        fun getInstance(context: Context): StoresLocalSettings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let { return it }
                StoresLocalSettings(context).also { INSTANCE = it }
            }
        }
    }
}
