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
package com.infomaniak.core.auth

import com.infomaniak.core.auth.networking.AuthHttpClientProvider
import com.infomaniak.core.network.ApiEnvironment
import com.infomaniak.core.network.NetworkConfiguration
import com.infomaniak.lib.login.InfomaniakLogin.AccessType

/**
 * AuthConfiguration : Allow to configure this module.
 */
object AuthConfiguration {

    lateinit var clientId: String
    internal var accessType: AccessType? = AccessType.OFFLINE
        private set

    fun init(
        appId: String,
        appVersionCode: Int,
        appVersionName: String,
        clientId: String,
        accessType: AccessType? = this.accessType,
        tokenInterceptorListener: TokenInterceptorListener,
        apiEnvironment: ApiEnvironment = ApiEnvironment.Prod,
    ) {
        this.clientId = clientId
        this.accessType = accessType
        AuthHttpClientProvider.tokenInterceptorListener = tokenInterceptorListener
        NetworkConfiguration.init(appId, appVersionCode, appVersionName, apiEnvironment)
    }
}
