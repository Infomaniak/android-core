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

import com.infomaniak.lib.login.InfomaniakLogin.AccessType

/**
 * AuthConfiguration : Allow to configure this module.
 */
object AuthConfiguration {

    lateinit var appId: String
    var appVersionCode: Int = -1
    lateinit var appVersionName: String
    lateinit var clientId: String
    var accessType: AccessType? = AccessType.OFFLINE

    fun init(
        appId: String,
        appVersionCode: Int,
        appVersionName: String,
        clientId: String,
    ) {
        this.appId = appId
        this.appVersionCode = appVersionCode
        this.appVersionName = appVersionName
        this.clientId = clientId
    }
}
