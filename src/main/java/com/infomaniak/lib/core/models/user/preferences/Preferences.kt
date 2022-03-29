/*
 * Infomaniak kDrive - Android
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
package com.infomaniak.lib.core.models.user.preferences

import android.os.Parcelable
import androidx.room.Embedded
import com.google.gson.annotations.SerializedName
import com.infomaniak.lib.core.models.user.preferences.security.Security
import kotlinx.parcelize.Parcelize

@Parcelize
data class Preferences(
    @Embedded(prefix = "security_")
    var security: Security?,
    @Embedded(prefix = "organizationPreference_")
    @SerializedName("account")
    var organizationPreference: OrganizationPreference,
    @Embedded(prefix = "language_")
    var language: Language,
    @Embedded(prefix = "country_")
    var country: Country,
    @Embedded(prefix = "timezone_")
    var timezone: TimeZone?,
) : Parcelable