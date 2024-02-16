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

import androidx.annotation.StyleRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.infomaniak.lib.core.networking.HttpClient
import com.infomaniak.lib.stores.updaterequired.UpdateRequiredActivity
import com.infomaniak.lib.stores.updaterequired.data.api.ApiRepositoryStores
import com.infomaniak.lib.stores.updaterequired.data.models.AppVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal interface StoresUtils {

    @Suppress("PropertyName")
    val REQUIRED_UPDATE_STORE: AppVersion.Store

    //region In-App Update
    fun FragmentActivity.checkUpdateIsRequired(
        appId: String,
        appVersion: String,
        versionCode: Int,
        @StyleRes themeRes: Int,
    ) = lifecycleScope.launch(Dispatchers.IO) {
        val apiResponse = ApiRepositoryStores.getAppVersion(appId, HttpClient.okHttpClientNoTokenInterceptor)

        if (apiResponse.data?.mustRequireUpdate(appVersion) == true) {
            withContext(Dispatchers.Main) {
                UpdateRequiredActivity.startUpdateRequiredActivity(this@checkUpdateIsRequired, appId, versionCode, themeRes)
                finish()
            }
        }
    }
    //endregion

    //region In-App Review
    fun FragmentActivity.launchInAppReview() = Unit
    //endregion
}
