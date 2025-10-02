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
package com.infomaniak.core.legacy.api

import com.infomaniak.core.legacy.BuildConfig.INFOMANIAK_API
import com.infomaniak.core.legacy.BuildConfig.INFOMANIAK_API_V1

object ApiRoutesCore {

    fun getUserProfile(): String {
        return "${INFOMANIAK_API}profile?no_avatar_default=1"
    }

    fun sendDeviceInfo(): String {
        return "${INFOMANIAK_API_V1}/devices"
    }
}
