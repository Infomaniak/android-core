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
package com.infomaniak.core.login.crossapp

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * ## Important message to editors
 *
 * For all existing Unless the given field never made it to a public release (which includes betas!):
 * - **NEVER** change the type of an existing field.
 * - **NEVER** change the value in the `@ProtoNumber` of an existing field.
 * - **NEVER** remove a field that was present before.
 * - **NEVER** reuse a protobuf number. Those shall be unique and stable, essentially forever.
 * - **Safe changes:**
 *    1. Renaming any existing field.
 *    2. Adding new fields with explicit `@ProtoNumber` using a new, never used number.
 *    3. Deprecating any field (without touching its `@ProtoNumber`).
 *    4. Rename this data class.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CrossAppDeviceIdRequest(
    @ProtoNumber(1)
    val packageNamesAlreadyBeingChecked: Set<String>
)
