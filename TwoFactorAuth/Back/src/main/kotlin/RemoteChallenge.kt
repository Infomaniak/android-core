/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.core.twofactorauth.back

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.Json

/**
 * # WARNING
 * In the [Json] config, make sure to enable those flags:
 * - [JsonConfiguration.coerceInputValues]
 * - [JsonConfiguration.ignoreUnknownKeys]
 * - [JsonConfiguration.decodeEnumsCaseInsensitive]
 *
 * @property type Type of challenge, `null` if unknown (and can therefore not be acted on).
 * @property createdAt UTC timestamp (seconds offset) of when the login request happened.
 * @property expiresAt UTC timestamp (seconds offset) of when the challenge expires.
 */
@ExperimentalUuidApi
@Serializable
data class RemoteChallenge(
    val uid: Uuid,
    val device: Device,
    val type: Type? = null,
    val location: Location,
    val createdAt: Long,
    val expiresAt: Long,
) {
    @Serializable
    enum class Type {
        Approval,
    }

    @Serializable
    data class Device(
        val name: String,
        val type: Type? = null,
    ) {
        @Serializable
        enum class Type {
            Phone,
            Tablet,
            Computer,
        }
    }

    @Serializable
    data class Location(
        val name: String,
        @SerialName("ip")
        val ipAddress: String,
    )
}
