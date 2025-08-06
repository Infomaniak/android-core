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
package com.infomaniak.core.crossapplogin.front.utils

import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import androidx.compose.ui.graphics.Color

internal fun TypedArray.getColorOrNull(@StyleableRes index: Int): Color? {
    return if (hasValue(index)) Color(getColor(index, -1)) else null
}

internal fun TypedArray.getStringOrNull(@StyleableRes index: Int): String? {
    return if (hasValue(index)) getString(index) else null
}
