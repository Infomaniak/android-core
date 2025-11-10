/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2025 Infomaniak Network SA
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

package com.infomaniak.core.notifications.registration

import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.serialization.Serializable
import splitties.init.appCtx

@ConsistentCopyVisibility
@Serializable
data class RegistrationInfo private constructor(
    val token: String,
    val name: String,
    val os: String,
    val model: String,
    val topics: List<String>,
) {

    companion object {

        suspend fun create(
            token: String,
            topics: List<String>,
        ): RegistrationInfo {
            val deviceModel = android.os.Build.MODEL
            val deviceName = Dispatchers.IO {
                Settings.Global.getString(appCtx.contentResolver, "device_name")
            }.let {
                // Some Xiaomi devices return null for this "device_name" so we need to check this case and not just "blank"
                if (it.isNullOrBlank()) deviceModel else it
            }
            return RegistrationInfo(
                token = token,
                name = deviceName,
                os = "android",
                model = deviceModel,
                topics = topics
            )
        }
    }
}
