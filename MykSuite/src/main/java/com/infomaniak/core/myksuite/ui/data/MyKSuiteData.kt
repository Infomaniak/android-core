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
package com.infomaniak.core.myksuite.ui.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.MyKSuiteTier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity
data class MyKSuiteData(
    @PrimaryKey val id: Int,
    @SerialName("trial_expiry_at")
    @ColumnInfo("trial_expiry_at")
    val trialExpiryAt: Long? = null,
    @SerialName("is_free")
    @ColumnInfo("is_free")
    val isFree: Boolean,
    @Embedded("drive_")
    val drive: KSuiteDrive,
    @Embedded("mail_")
    val mail: KSuiteMail,
) {

    @Transient
    @ColumnInfo("user_id")
    var userId: Int = 0

    inline val name get() = if (isFree) R.string.myKSuiteName else R.string.myKSuitePlusName
    inline val tier get() = if (isFree) MyKSuiteTier.Free else MyKSuiteTier.Plus
    inline val trialExpiryAtMillisecond get() = trialExpiryAt?.times(1_000)
}
