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
package com.infomaniak.core.myksuite.ui.network

import androidx.room.ColumnInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KSuitePack(
    val id: Int,
    val name: String = "",
    @SerialName("drive_storage")
    @ColumnInfo("drive_storage")
    val driveStorage: Long = 0,
    @SerialName("mail_storage")
    @ColumnInfo("mail_storage")
    val mailStorage: Long = 0,
    @SerialName("mail_daily_limit_send")
    @ColumnInfo("mail_daily_limit_send")
    val mailDailyLimitSend: Int = 0,
    @SerialName("is_max_storage_offer")
    @ColumnInfo("is_max_storage_offer")
    val isMaxStorageOffer: Boolean = false,
)
