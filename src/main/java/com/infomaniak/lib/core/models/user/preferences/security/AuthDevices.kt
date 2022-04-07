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
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthDevices(
    var id: Int,
    var name: String,
    @SerializedName("last_connexion")
    var last_connexion: Long,
    @SerializedName("user_agent")
    var user_agent: String,
    @SerializedName("user_ip")
    var user_ip: String,
    var device: String,
    @SerializedName("created_at")
    var created_at: Long,
    @SerializedName("updated_at")
    var updated_at: Long,
    @SerializedName("deleted_at")
    var deleted_at: Long?,
) : Parcelable