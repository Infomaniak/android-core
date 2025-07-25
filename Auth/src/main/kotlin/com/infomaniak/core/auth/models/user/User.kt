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
package com.infomaniak.core.auth.models.user

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.infomaniak.core.auth.firstOrEmpty
import com.infomaniak.core.auth.models.OrganizationAccount
import com.infomaniak.core.auth.models.user.preferences.Preferences
import com.infomaniak.lib.login.ApiToken
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class User(
    @PrimaryKey val id: Int,
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("first_name")
    val firstname: String,
    @SerializedName("last_name")
    val lastname: String,
    val email: String,
    val avatar: String?,
    val login: String,
    @ColumnInfo(defaultValue = "false")
    @SerializedName("is_staff")
    val isStaff: Boolean,
    @Embedded(prefix = "preferences_")
    val preferences: Preferences,
    val phones: ArrayList<Phone>?,
    val emails: ArrayList<Email>?,

    /**
     * Local
     */
    @Embedded
    var apiToken: ApiToken,
    var organizations: ArrayList<OrganizationAccount>

) : Parcelable {
    fun getInitials() = "${firstname.firstOrEmpty().uppercase()}${lastname.firstOrEmpty().uppercase()}"
}
