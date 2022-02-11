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
package com.infomaniak.lib.core.models

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.infomaniak.lib.core.utils.firstOrEmpty
import com.infomaniak.lib.login.ApiToken
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class User(
    @PrimaryKey val id: Int,
    @Embedded var apiToken: ApiToken,
    val avatar: String?,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("double_auth") val doubleAuth: Boolean,
    @SerializedName("double_auth_method") val doubleAuthMethod: String?,
    val email: String,
    @SerializedName("email_reminder_validate") val emailReminderValidate: String?,
    @SerializedName("email_validate") val emailValidate: String?,
    val emails: ArrayList<Email>?,
    val firstname: String,
    val lastname: String,
    val login: String,
    @SerializedName("phone_reminder_validate") val phoneReminderValidate: String?,
    val phones: ArrayList<Phone>?,
    var organizations: ArrayList<OrganizationAccount>
) : Parcelable {
    fun getInitials() = "${firstname.firstOrEmpty()}${lastname.firstOrEmpty()}"
}