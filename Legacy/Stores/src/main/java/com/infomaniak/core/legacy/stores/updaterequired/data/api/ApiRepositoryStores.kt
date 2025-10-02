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
package com.infomaniak.core.legacy.stores.updaterequired.data.api

import com.infomaniak.core.legacy.api.ApiController
import com.infomaniak.core.legacy.api.ApiController.ApiMethod
import com.infomaniak.core.legacy.models.ApiResponse
import com.infomaniak.core.legacy.stores.updaterequired.data.models.AppVersion
import okhttp3.OkHttpClient

object ApiRepositoryStores {

    suspend fun getAppVersion(appName: String, okHttpClient: OkHttpClient): ApiResponse<AppVersion> {
        return ApiController.callApi(ApiRoutesStores.appVersion(appName), ApiMethod.GET, okHttpClient = okHttpClient)
    }
}
