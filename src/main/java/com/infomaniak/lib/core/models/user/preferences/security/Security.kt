/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
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
package com.infomaniak.lib.core.models.user.preferences.security

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Security(
    @ColumnInfo(defaultValue = "0")
    var score: Int,
    @SerializedName("has_recovery_email")
    @ColumnInfo(defaultValue = "false")
    var has_recovery_email: Boolean,
    @SerializedName("has_valid_phone")
    @ColumnInfo(defaultValue = "false")
    var has_valid_phone: Boolean,
    @SerializedName("email_validated_at")
    @ColumnInfo(defaultValue = "0")
    var email_validated_at: Long,
    @ColumnInfo(defaultValue = "false")
    var otp: Boolean,
    @ColumnInfo(defaultValue = "false")
    var sms: Boolean,
    @SerializedName("sms_phone")
    @ColumnInfo(defaultValue = "")
    var sms_phone: String,
    @ColumnInfo(defaultValue = "false")
    var yubikey: Boolean,
    @SerializedName("infomaniak_application")
    @ColumnInfo(defaultValue = "false")
    var infomaniak_application: Boolean,
    @SerializedName("double_auth")
    @ColumnInfo(defaultValue = "false")
    var double_auth: Boolean,
    @SerializedName("remaining_rescue_code")
    @ColumnInfo(defaultValue = "0")
    var remaining_rescue_code: Int,
    @SerializedName("last_login_at")
    @ColumnInfo(defaultValue = "0")
    var last_login_at: Long,
    @SerializedName("date_last_changed_password")
    @ColumnInfo(defaultValue = "0")
    var date_last_changed_password: Long,
    @SerializedName("double_auth_method")
    @ColumnInfo(defaultValue = "")
    var double_auth_method: String,
    @SerializedName("auth_devices")
    var authDevices: ArrayList<AuthDevices>?,
) : Parcelable