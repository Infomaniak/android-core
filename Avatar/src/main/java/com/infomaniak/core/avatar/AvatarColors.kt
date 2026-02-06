/*
 * Infomaniak Core - Android
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
package com.infomaniak.core.avatar

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAvatarColors = staticCompositionLocalOf<AvatarColors> {
    AvatarColors(
        containerColors = listOf(Color(0xFF30ABFF), Color(0xFF3CB572), Color(0xFFED2C6E)),
        contentColor = Color(0xFFFFFFFF),
    )
}

data class AvatarColors(
    val containerColors: List<Color>, // The colorful background behind the initials
    val contentColor: Color, // The color of the initials drawn on top of the colored background
)
