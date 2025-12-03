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
package com.infomaniak.core.network

import com.infomaniak.core.network.utils.ErrorCodeTranslated
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import splitties.toast.UnreliableToastApi
import splitties.toast.toast

/**
 * NetworkConfiguration : Allow to configure this module.
 */
object NetworkConfiguration {

    lateinit var appId: String
    var appVersionCode: Int = -1
    lateinit var appVersionName: String
    var customHeaders: MutableMap<String, String>? = null
    var apiErrorCodes: List<ErrorCodeTranslated>? = null
        private set

    fun init(
        appId: String,
        appVersionCode: Int,
        appVersionName: String,
        apiEnvironment: ApiEnvironment = ApiEnvironment.Prod,
        apiErrorCodes: List<ErrorCodeTranslated>? = this.apiErrorCodes,
    ) {
        this.appId = appId
        this.appVersionCode = appVersionCode
        this.appVersionName = appVersionName
        ApiEnvironment.current = apiEnvironment
        this.apiErrorCodes = apiErrorCodes

        @OptIn(DelicateCoroutinesApi::class) // No need for structured concurrency here
        if (BuildConfig.DEBUG || apiEnvironment != ApiEnvironment.Prod) GlobalScope.launch(Dispatchers.Main) {
            yield() // Needed to ensure the underlying context of applicationContext (used in toast) is initialized

            // Show which API environment is being used on process creation.
            val apiHostMessage = when (apiEnvironment) {
                ApiEnvironment.Prod -> "api host: Prod"
                ApiEnvironment.PreProd -> "api host: Preprod"
                is ApiEnvironment.Custom -> "api host: ${apiEnvironment.host}"
            }
            @OptIn(UnreliableToastApi::class)
            toast(apiHostMessage)
        }
    }
}
