/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core

import com.infomaniak.lib.core.auth.CredentialManager

/**
 * InfomaniakCore : Allow to instantiate the library with some parameters
 */
object InfomaniakCore {
    lateinit var appVersionName: String
    lateinit var bearerToken: String
    var appVersionCode: Int = -1
    var clientId: String? = null
    var credentialManager: CredentialManager? = null
    var isDebug: Boolean = false

    fun init(
        appVersionCode: Int = -1,
        appVersionName: String,
        bearerToken: String = "",
        clientId: String? = null,
        credentialManager: CredentialManager? = null,
        isDebug: Boolean = false
    ) {
        this.appVersionCode = appVersionCode
        this.appVersionName = appVersionName
        this.bearerToken = bearerToken
        this.clientId = clientId
        this.credentialManager = credentialManager
        this.isDebug = isDebug
    }
}
