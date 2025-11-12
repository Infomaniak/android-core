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
package com.infomaniak.core.appversionchecker.data.api

import com.infomaniak.core.appversionchecker.data.models.AppVersion
import com.infomaniak.core.network.api.ApiController
import com.infomaniak.core.network.api.ApiController.ApiMethod
import com.infomaniak.core.network.models.ApiResponse
import okhttp3.OkHttpClient

object ApiRepositoryStores {
    suspend fun getAppVersion(
        appName: String,
        store: AppVersion.Store,
        okHttpClient: OkHttpClient
    ): ApiResponse<AppVersion> {
        return ApiController.callApi(ApiRoutesStores.appVersion(appName, store), ApiMethod.GET, okHttpClient = okHttpClient)
    }

    /**
     * Get app version with projection to have a lighter JSON
     *
     * @param appName: App package name
     * @param store: Specify which store is check
     * @param projectionFields: A List of fields to keep to build response
     * @param channelFilter: A optional parameter to filter by distribution channel.
     * @param okHttpClient
     */
    suspend fun getAppVersion(
        appName: String,
        store: AppVersion.Store,
        projectionFields: List<AppVersion.ProjectionFields>,
        channelFilter: AppVersion.VersionChannel,
        okHttpClient: OkHttpClient
    ): ApiResponse<AppVersion> {
        return ApiController.callApi(
            url = ApiRoutesStores.appVersion(appName, store, projectionFields, channelFilter),
            method = ApiMethod.GET,
            okHttpClient = okHttpClient,
            useKotlinxSerialization = true
        )
    }
}
