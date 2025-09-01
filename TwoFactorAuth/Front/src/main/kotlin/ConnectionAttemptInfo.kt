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
package com.infomaniak.core.twofactorauth.front

import kotlinx.serialization.SerialName

data class ConnectionAttemptInfo(
    val targetAccount: TargetAccount,
    val deviceOrBrowserName: String,
    val deviceType: DeviceType,
    val location: String,
) {
    data class TargetAccount(
        val avatarUrl: String?,
        val fullName: String,
        val initials: String,
        val email: String,
        val id: Long,
    )

    enum class DeviceType {
        @SerialName("phone")
        Phone,
        @SerialName("tablet")
        Tablet,
        @SerialName("computer")
        Computer,
    }
}
