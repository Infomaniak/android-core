/*
 * Infomaniak Authenticator - Android
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
package com.infomaniak.core.avatar.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Avatars light
val yellow_light = Color(0xFFFFC10A)
val coral_light = Color(0xFFFF540B)
val grass_light = Color(0xFF3EBF4D)
val fougere_light = Color(0xFF009688)
val cobalt_light = Color(0xFF006AB2)
val jean_light = Color(0xFF2196F3)
val tropical_light = Color(0xFF00BCD4)
val mauve_light = Color(0xFFBA68C8)
val prince_light = Color(0xFF673AB7)

// Avatars dark
val yellow_dark = Color(0xFFFFDF9E)
val coral_dark = Color(0xFFFD9459)
val grass_dark = Color(0xFF78D383)
val fougere_dark = Color(0xFF00C7B3)
val cobalt_dark = Color(0xFF0AB2F5)
val jean_dark = Color(0xFF81C4F8)
val tropical_dark = Color(0xFF86DEEA)
val mauve_dark = Color(0xFFCC8FD6)
val prince_dark = Color(0xFF956DDD)

@Immutable
class AvatarColorsScheme(
    yellow: Color,
    coral: Color,
    grass: Color,
    fougere: Color,
    cobalt: Color,
    jean: Color,
    tropical: Color,
    mauve: Color,
    prince: Color,
) {
    val colorList = listOf(yellow, coral, grass, fougere, cobalt, jean, tropical, mauve, prince)
}

val lightAvatarColorsScheme = AvatarColorsScheme(
    yellow = yellow_light,
    coral = coral_light,
    grass = grass_light,
    fougere = fougere_light,
    cobalt = cobalt_light,
    jean = jean_light,
    tropical = tropical_light,
    mauve = mauve_light,
    prince = prince_light,
)

val darkAvatarColorsScheme = AvatarColorsScheme(
    yellow = yellow_dark,
    coral = coral_dark,
    grass = grass_dark,
    fougere = fougere_dark,
    cobalt = cobalt_dark,
    jean = jean_dark,
    tropical = tropical_dark,
    mauve = mauve_dark,
    prince = prince_dark,
)
