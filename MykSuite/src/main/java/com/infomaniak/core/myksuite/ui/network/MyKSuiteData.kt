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
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class MyKSuiteData(
    @PrimaryKey val id: Int,
    val status: String,
    @SerialName("pack_id")
    @ColumnInfo("pack_id")
    val kSuitePackId: Int,
    @SerialName("pack")
    @ColumnInfo("k_suite_pack_")
    @Embedded("k_suite_pack_")
    val kSuitePack: KSuitePack? = null,
    @SerialName("is_free")
    @ColumnInfo("is_free")
    val isFree: Boolean,
    @Embedded("drive_")
    val drive: KSuiteDrive? = null,
    @SerialName("free_mail")
    @ColumnInfo("free_mail")
    @Embedded("free_mail_")
    val freeMail: KSuiteFreeMail? = null,
    @SerialName("has_auto_renew")
    @ColumnInfo("has_auto_renew")
    val hasAutoRenew: Boolean,
)
