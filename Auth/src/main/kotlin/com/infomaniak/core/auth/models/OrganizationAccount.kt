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
package com.infomaniak.core.auth.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class OrganizationAccount(
    val id: Int,
    val name: String,
    val type: Type,
    val billing: Boolean,
    val mailing: Boolean,
    @SerializedName("no_access") @SerialName("no_access")
    val noAccess: Boolean,
    @SerializedName("workspace_only") @SerialName("workspace_only")
    val workspaceOnly: Boolean,
    @SerializedName("billing_mailing") @SerialName("billing_mailing")
    val billingMailing: Boolean,
    @SerializedName("legal_entity_type") @SerialName("legal_entity_type")
    val legalEntityType: String
) : Parcelable {

    @Serializable
    enum class Type {
        @SerializedName("owner") @SerialName("owner")
        OWNER,

        @SerializedName("admin") @SerialName("admin")
        ADMIN,

        @SerializedName("normal") @SerialName("normal")
        NORMAL,

        @SerializedName("client") @SerialName("client")
        CLIENT;
    }
}
