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
package com.infomaniak.lib.core

import com.infomaniak.lib.core.auth.CredentialManager
import com.infomaniak.lib.core.utils.ErrorCodeTranslated
import com.infomaniak.lib.login.InfomaniakLogin.AccessType
import java.net.URLEncoder

/**
 * InfomaniakCore : Allow to instantiate the library with some parameters
 */
object InfomaniakCore {

    lateinit var appId: String
    var appVersionCode: Int = -1
    lateinit var appVersionName: String
    lateinit var clientId: String
    var credentialManager: CredentialManager? = null
    var customHeaders: MutableSet<CustomHeader> = mutableSetOf()
    var apiErrorCodes: List<ErrorCodeTranslated>? = null
    var accessType: AccessType? = AccessType.OFFLINE

    fun init(appId: String, appVersionCode: Int, appVersionName: String, clientId: String) {
        this.appId = appId
        this.appVersionCode = appVersionCode
        this.appVersionName = appVersionName
        this.clientId = clientId
    }
}

/**
 *  CustomHeader : Represents a custom HTTP header allows defining how the header value should be handled.
 */
sealed interface CustomHeader {
    val headerKey: String
    val headerValue: String

    data class AutoEncodeHeaderToUTF8(override val headerKey: String, private val rawValue: String) : CustomHeader {
        override val headerValue: String = URLEncoder.encode(rawValue, "UTF-8")
    }

    data class RawHeader(override val headerKey: String, override val headerValue: String) : CustomHeader
}
