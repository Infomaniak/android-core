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

import androidx.annotation.ColorInt
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// TODO: Use the right colors.
// Palette
private const val green_dark = 0xFF014958
private const val green_main = 0xFF3CB572
private const val green_secondary = 0xFFE3F6DC
private const val green_contrast = 0xFFCAF769

private const val orca = 0xFF333333
private const val elephant = 0xFF666666
private const val shark = 0xFF9F9F9F
private const val mouse = 0xFFE0E0E0
private const val polar_bear = 0xFFF5F5F5
private const val rabbit = 0xFFF1F1F1

private const val specific1 = 0xFFD8F0E3
private const val specific2 = 0xFFCCDBDE
private const val specific3 = 0xFFF5ECD1
private const val specific4 = 0xFFCF9E1B

// Extra palette
private const val on_primary = 0xFFF7FCFA
private const val white = 0xFFFFFFFF
private const val black_translucent = 0x80000000

private const val warning = 0xFFFF8500
private const val error = 0xFFF44336

/**
 * Try to not do that.
 * This is an exceptional use case because this color is used outside of Compose
 * (hence we can't access the Compose color schemes).
 */
@ColorInt
const val notificationIconColor: Int = green_main.toInt()

val LightColorScheme = lightColorScheme(
    primary = Color(green_main),
    onPrimary = Color(on_primary),
    secondaryContainer = Color(green_secondary),
    onSecondaryContainer = Color(green_main),
    tertiary = Color(green_dark),
    onTertiary = Color(green_contrast),

    background = Color(white),
    surface = Color(white), // Same value as background
    onSurface = Color(green_main),
    onSurfaceVariant = Color(green_dark),
    surfaceContainerLow = Color(white), // Same value as background. Used for bottom sheet backgrounds
    surfaceContainerHighest = Color(polar_bear), // Used for filled cards backgrounds

    outline = Color(mouse), // Used for TextField's border
    outlineVariant = Color(mouse), // Used for divider's color

    error = Color(error),
    // onError: uses default values
)

val CustomLightColorScheme = CustomColorScheme(
    primaryTextColor = Color(orca),
    secondaryTextColor = Color(elephant),
    tertiaryTextColor = Color(shark),
    iconColor = Color(elephant),
    navigationItemBackground = LightColorScheme.background,
    tertiaryButtonBackground = Color(rabbit),
    warning = Color(warning),
)
