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
package com.infomaniak.core.myksuite.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// TODO: Extract colors to a CoreUi module

// Palette
private const val orca = 0xFF333333
private const val elephant = 0xFF666666
private const val shark = 0xFF9F9F9F
private const val mouse = 0xFFE0E0E0
private const val polar_bear = 0xFFF5F5F5
private const val rabbit = 0xFFF1F1F1

// Extra Palette
private const val white = 0xFFFFFFFF
private const val sky = 0xFFF4F6FD

// KSuite App colors
private const val infomaniak = 0xFF0098FF
private const val mailButton = 0xFFBC0055
private const val onMailButton = 0xFFFEF8F7
private const val mail = 0xFFF2357A
private const val drive = 0xFF5C89F7

internal fun getLightColorScheme(primaryColor: Color, onPrimaryColor: Color) = lightColorScheme(
    primary = primaryColor,
    onPrimary = onPrimaryColor,
    onSurfaceVariant = Color(mouse), // Used for bottom sheet drag handle
    surfaceContainerLow = Color(white), // Used for bottom sheet backgrounds
    surfaceContainerHighest = Color(sky), // Used for Card backgrounds
    outlineVariant = Color(mouse), // Used for divider's color
)

internal val MyKSuiteLightColors = MyKSuiteColors(
    primaryTextColor = Color(orca),
    secondaryTextColor = Color(elephant),
    tertiaryTextColor = Color(shark),
    background = Color(white),
    secondaryBackground = Color(white),
    topAppBarBackground = Color(sky),
    informationBlockBackground = Color(polar_bear),
    chipBackground = Color(rabbit),
    drive = Color(drive),
    mail = Color(mail),
    primaryButton = Color(infomaniak),
    onPrimaryButton = Color(white),
    mailButton = Color(mailButton),
    onMailButton = Color(onMailButton),
    iconColor = Color(elephant),
)
