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
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val id: Int?,
    val city: String?,
    val type: AddressType,
    val title: String?,
    val street: String?,
    val country: String?,
    @SerializedName("country_id")
    val countryId: Int?,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("default")
    var isDefault: Boolean?,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("vat_number")
    val vatNumber: String?,
    @SerializedName("organization_name")
    val organizationName: String?,
    @SerializedName("street_complement")
    val streetComplement: String?,
    @SerializedName("zip_code", alternate = ["zip"])
    val zipCode: String?
) : Parcelable {
    enum class AddressType {
        @SerializedName("individual", alternate = ["personal", "professional"])
        INDIVIDUAL,

        @SerializedName("organization")
        ORGANIZATION
    }

    fun getDisplayedTitle(): String {
        return if (type == AddressType.ORGANIZATION) {
            organizationName ?: ""
        } else {
            String.format("%s %s", firstName, lastName)
        }
    }
}
