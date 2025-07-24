/*
 * Infomaniak Login - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.infomaniak.core.login.crossapp

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * ## Important message to editors
 *
 * For all existing fields, unless the given field never made it to a public release (which includes betas!):
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
data class ExternalAccount(
    @ProtoNumber(1)
    val id: Long,
    @ProtoNumber(2)
    val fullName: String,
    @ProtoNumber(3)
    val initials: String,
    @ProtoNumber(4)
    val email: String,
    @ProtoNumber(5)
    val avatarUrl: String? = null,
    @ProtoNumber(6)
    val isCurrentlySelectedInAnApp: Boolean,
    @ProtoNumber(7)
    val tokens: Set<String>,
)
