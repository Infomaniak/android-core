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
package com.infomaniak.core.ksuite.myksuite.data

import androidx.room.ColumnInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KSuiteMail(
    val id: Int,
    val email: String,
    @SerialName("daily_limit_sent")
    @ColumnInfo("daily_limit_sent")
    val dailyLimitSent: Int,
    @SerialName("storage_size_limit")
    @ColumnInfo("storage_size_limit")
    val storageSizeLimit: Long,
    @SerialName("used_size")
    @ColumnInfo("used_size")
    val usedSize: Long,
    @SerialName("mailbox_id")
    @ColumnInfo("mailbox_id")
    val mailboxId: Int,
)
