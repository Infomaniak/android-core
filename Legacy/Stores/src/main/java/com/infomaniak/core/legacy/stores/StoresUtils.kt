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
package com.infomaniak.core.legacy.stores

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.annotation.StyleRes
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.appversionchecker.data.api.ApiRepositoryStores
import com.infomaniak.core.appversionchecker.data.models.AppVersion
import com.infomaniak.core.legacy.networking.HttpClient
import com.infomaniak.core.legacy.stores.updaterequired.UpdateRequiredActivity
import kotlinx.coroutines.launch

internal interface StoresUtils {

    @Suppress("PropertyName")
    val REQUIRED_UPDATE_STORE: AppVersion.Store

    //region In-App Update
    fun FragmentActivity.checkUpdateIsRequired(
        appId: String,
        appVersion: String,
        versionCode: Int,
        @StyleRes themeRes: Int,
    ) = lifecycleScope.launch {
        val projectionFields = listOf(
            AppVersion.ProjectionFields.MinVersion,
            AppVersion.ProjectionFields.PublishedVersionsTag,
        )

        val apiResponse = ApiRepositoryStores.getAppVersion(
            appName = appId,
            store = REQUIRED_UPDATE_STORE,
            projectionFields = projectionFields,
            channelFilter = AppVersion.VersionChannel.Production,
            okHttpClient = HttpClient.okHttpClient
        )

        if (apiResponse.data?.mustRequireUpdate(appVersion) == true) {
            UpdateRequiredActivity.startUpdateRequiredActivity(this@checkUpdateIsRequired, appId, versionCode, themeRes)
            finish()
        }
    }
    //endregion

    //region In-App Review
    fun FragmentActivity.launchInAppReview() = Unit
    //endregion

    //region Context Extensions
    // TODO: When the Stores module will be moved from Legacy to the new Core, we'll be able to remove this function.
    /**
     * Historically, `market://` is only for the Google PlayStore, but F-Droid handles it too.
     */
    fun Context.goToAppStore(appPackageName: String = packageName) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri()))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$appPackageName".toUri()))
        }
    }
    //endregion
}
