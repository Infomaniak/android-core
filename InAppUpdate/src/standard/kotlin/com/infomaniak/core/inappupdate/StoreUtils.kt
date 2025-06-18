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
package com.infomaniak.core.inappupdate

import androidx.annotation.StyleRes
import com.google.android.play.core.install.model.AppUpdateType
import com.infomaniak.core.inappupdate.updaterequired.data.models.AppVersion.Store

object StoreUtils : StoresUtils {
    const val APP_UPDATE_TAG = "inAppUpdate"
    const val DEFAULT_UPDATE_TYPE = AppUpdateType.FLEXIBLE
    override val REQUIRED_UPDATE_STORE = Store.PLAY_STORE

    override suspend fun checkUpdateIsRequired(
        appId: String,
        appVersion: String,
        versionCode: Int,
        @StyleRes themeRes: Int,
    ) {
        super.checkUpdateIsRequired(
            appId,
            appVersion,
            versionCode,
            themeRes,
        )
    }
}
