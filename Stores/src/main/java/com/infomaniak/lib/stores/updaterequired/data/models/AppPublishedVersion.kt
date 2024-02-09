/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.lib.stores.updaterequired.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class AppPublishedVersion(
    var tag: String,
    @SerializedName("tag_updated_at")
    var tagUpdatedAt: Date,
    @SerializedName("version_changelog")
    var versionChangelog: String,
    var type: VersionType,
    @SerializedName("build_version")
    var buildVersion: String,
    @SerializedName("build_min_os_version")
    var buildMinOsVersion: String,
) {
    enum class VersionType(val apiValue: String) {
        PRODUCTION("production"),
        BETA("beta"),
    }
}
