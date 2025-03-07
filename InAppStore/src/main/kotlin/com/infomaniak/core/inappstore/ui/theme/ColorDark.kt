/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2025 Infomaniak Network SA
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
package com.infomaniak.core.inappstore.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// TODO: Use the right colors.
// Palette
private const val green_dark = 0xFF014958
private const val green_main = 0xFF67DD95

private const val dark1 = 0xFF152123
private const val dark2 = 0xFF2B383B
private const val dark3 = 0xFF3C4F52
private const val shark = 0xFF9F9F9F
private const val rabbit = 0xFFF1F1F1

// Extra palette
private const val elephant = 0xFF666666

private const val warning = 0xFFFFAA4C
private const val error = 0xFFFC8878

val DarkColorScheme = darkColorScheme(
    primary = Color(green_main),
    onPrimary = Color(dark1),
    secondaryContainer = Color(dark3),
    onSecondaryContainer = Color(green_main),
    tertiary = Color(green_dark),
    onTertiary = Color(green_main),

    background = Color(dark1),
    surface = Color(dark1), // Same value as background
    onSurface = Color(green_main),
    onSurfaceVariant = Color(rabbit),
    surfaceContainerLow = Color(dark2), // Used for bottom sheet backgrounds
    surfaceContainerHighest = Color(dark2), // Used for filled cards backgrounds

    outline = Color(dark3), // Used for TextField's border
    outlineVariant = Color(dark3), // Used for divider's color

    error = Color(error),
    // onError: uses default  values
)

val CustomDarkColorScheme = CustomColorScheme(
    primaryTextColor = Color(rabbit),
    secondaryTextColor = Color(shark),
    tertiaryTextColor = Color(elephant),
    iconColor = Color(shark),
    navigationItemBackground = Color(dark2),
    tertiaryButtonBackground = Color(dark2),
    warning = Color(warning),
)
