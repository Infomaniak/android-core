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
package com.infomaniak.core.avatar

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color

@ColorInt
fun Context.getBackgroundColorResBasedOnId(id: Int, @ArrayRes array: Int? = null): Int {
    val arrayResource = array ?: R.array.organizationColors
    val colors = resources.getIntArray(arrayResource).toList()
    return chooseColorForId(id, colors)
}

fun getBackgroundColorResBasedOnId(id: Int, colors: List<Color>): Color {
    return chooseColorForId(id, colors)
}

private fun <T> chooseColorForId(id: Int, items: List<T>): T = items[Math.floorMod(id, items.size)]
