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
package com.infomaniak.core.myksuite.ui.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyKSuiteData(
    val id: Int,
    val status: String,
    @SerialName("pack_id")
    val kSuitePackId: Int,
    @SerialName("pack")
    val kSuitePack: KSuitePack? = null,
    @SerialName("is_free")
    val isFree: Boolean,
    val drive: KSuiteDrive? = null,
    @SerialName("free_mail")
    val freeMail: KSuiteFreeMail? = null,
    @SerialName("has_auto_renew")
    val hasAutoRenew: Boolean,
)
