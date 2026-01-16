/*
 * Infomaniak SwissTransfer - Android
 * Copyright (C) 2026 Infomaniak Network SA
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

import androidx.room.Entity
import androidx.room.PrimaryKey

const val CURRENT_USER_ID_UNIQUE_KEY = "CURRENT_USER_ID_UNIQUE_KEY"

@Entity
data class CurrentUserId(
    val id: Int?,
) {
    @PrimaryKey
    private val uniqueKey: String = CURRENT_USER_ID_UNIQUE_KEY
}
