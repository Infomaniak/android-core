/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2025 Infomaniak Network SA
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
package com.infomaniak.core.inappstore

import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.inappstore.updaterequired.UpdateRequiredActivity
import com.infomaniak.core.inappstore.updaterequired.data.models.AppVersion
import com.infomaniak.core.inappstore.updaterequired.data.models.AppVersion.Platform
import com.infomaniak.inappstore.StoreUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal interface StoresUtils {

    @Suppress("PropertyName")
    val REQUIRED_UPDATE_STORE: AppVersion.Store

    private fun appVersion(appName: String): String {
        val store = StoreUtils.REQUIRED_UPDATE_STORE.apiValue
        val platform = Platform.ANDROID.apiValue

        val parameters = "?only=min_version,published_versions.tag&filter_versions[]=production"

        return "${BuildConfig.INFOMANIAK_API_V1}/app-information/versions/$store/$platform/$appName$parameters"
    }

    //region In-App Update
    fun FragmentActivity.checkUpdateIsRequired(
        appId: String,
        appVersion: String,
        versionCode: Int,
        @StyleRes themeRes: Int,
        @DrawableRes appIllustration: Int,
    ) = lifecycleScope.launch(Dispatchers.IO) {

        val apiResponse = HttpClient().get(appVersion(appId))
        val data = apiResponse.body<AppVersion>()

        if (data.mustRequireUpdate(appVersion)) {
            withContext(Dispatchers.Main) {
                UpdateRequiredActivity.startUpdateRequiredActivity(
                    this@checkUpdateIsRequired,
                    appId,
                    versionCode,
                    themeRes,
                    appIllustration,
                )
                finish()
            }
        }
    }
    //endregion

    //region In-App Review
    fun FragmentActivity.launchInAppReview() = Unit
    //endregion
}
