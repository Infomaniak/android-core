/*
 * Infomaniak Core - Android
 * Copyright (C) 2023 Infomaniak Network SA
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
    var hasRecoveryEmail: Boolean,
    @SerializedName("has_valid_phone")
    @ColumnInfo(defaultValue = "false")
    var hasValidPhone: Boolean,
    @SerializedName("email_validated_at")
    @ColumnInfo(defaultValue = "0")
    var emailValidatedAt: Long,
    @ColumnInfo(defaultValue = "false")
    var otp: Boolean,
    @ColumnInfo(defaultValue = "false")
    var sms: Boolean,
    @SerializedName("sms_phone")
    @ColumnInfo(defaultValue = "")
    var smsPhone: String,
    @ColumnInfo(defaultValue = "false")
    var yubikey: Boolean,
    @SerializedName("infomaniak_application")
    @ColumnInfo(defaultValue = "false")
    var infomaniakApplication: Boolean,
    @SerializedName("double_auth")
    @ColumnInfo(defaultValue = "false")
    var doubleAuth: Boolean,
    @SerializedName("remaining_rescue_code")
    @ColumnInfo(defaultValue = "0")
    var remainingRescueCode: Int,
    @SerializedName("last_login_at")
    @ColumnInfo(defaultValue = "0")
    var lastLoginAt: Long,
    @SerializedName("date_last_changed_password")
    @ColumnInfo(defaultValue = "0")
    var dateLastChangedPassword: Long,
    @SerializedName("double_auth_method")
    @ColumnInfo(defaultValue = "")
    var doubleAuthMethod: String,
    @SerializedName("auth_devices")
    var authDevices: ArrayList<AuthDevices>?,
) : Parcelable
