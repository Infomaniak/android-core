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
package com.infomaniak.core.legacy.models

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.infomaniak.core.legacy.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrganizationAccount(
    val id: Int,
    val name: String,
    val type: Type,
    val billing: Boolean,
    val mailing: Boolean,
    @SerializedName("no_access")
    val noAccess: Boolean,
    @SerializedName("workspace_only")
    val workspaceOnly: Boolean,
    @SerializedName("billing_mailing")
    val billingMailing: Boolean,
    @SerializedName("legal_entity_type")
    val legalEntityType: String
) : Parcelable {

    enum class Type {
        @SerializedName("owner")
        OWNER,

        @SerializedName("admin")
        ADMIN,

        @SerializedName("normal")
        NORMAL,

        @SerializedName("client")
        CLIENT;

        fun translate(context: Context) = when (this) {
            OWNER -> context.getString(R.string.typeOwner)
            ADMIN -> context.getString(R.string.typeAdmin)
            NORMAL -> context.getString(R.string.typeNormal)
            CLIENT -> context.getString(R.string.typeClient)
        }
    }
}
