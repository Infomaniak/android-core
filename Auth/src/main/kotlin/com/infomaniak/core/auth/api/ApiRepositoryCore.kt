/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2025 Infomaniak Network SA
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
package com.infomaniak.core.auth.api

import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.network.api.ApiController
import com.infomaniak.core.network.models.ApiResponse
import okhttp3.OkHttpClient

abstract class ApiRepositoryCore {

    suspend fun getUserProfile(
        okHttpClient: OkHttpClient,
        withEmails: Boolean = false,
        withPhones: Boolean = false,
        withSecurity: Boolean = false,
    ): ApiResponse<User> = ApiRepositoryCore.getUserProfile(okHttpClient, withEmails, withPhones, withSecurity)

    companion object {
        suspend fun getUserProfile(
            okHttpClient: OkHttpClient,
            withEmails: Boolean = false,
            withPhones: Boolean = false,
            withSecurity: Boolean = false,
        ): ApiResponse<User> {
            var with = ""
            if (withEmails) with += "emails"
            if (withPhones) with += "phones"
            if (withSecurity) with += "security"
            if (with.isNotEmpty()) with = "?with=$with"

            val url = "${ApiRoutesCore.getUserProfile()}$with"
            return ApiController.callApi(url, ApiController.ApiMethod.GET, okHttpClient = okHttpClient)
        }
    }
}
