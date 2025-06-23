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
package com.infomaniak.core.network.utils

import androidx.core.os.LocaleListCompat
import java.util.Locale

object Utils {
    fun getPreferredLocaleList(): List<Locale> {
        val adjustedLocaleListCompat = LocaleListCompat.getAdjustedDefault()
        val preferredLocaleList = mutableListOf<Locale>()
        for (index in 0 until adjustedLocaleListCompat.size()) {
            preferredLocaleList.add(adjustedLocaleListCompat[index]!!)
        }
        return preferredLocaleList
    }
}
