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
package com.infomaniak.lib.myksuite.ui.theme

import androidx.compose.ui.graphics.Color

// TODO: Extract colors to a CoreUi module

// Palette
private const val orca = 0xFF333333
private const val elephant = 0xFF666666
private const val shark = 0xFF9F9F9F
private const val mouse = 0xFFE0E0E0
private const val polar_bear = 0xFFF5F5F5
private const val rabbit = 0xFFF1F1F1

// KSuite App colors
private const val infomaniak = 0xFF0098FF
private const val mailButton = 0xFFBC0055
private const val mail = 0xFFF2357A
private const val drive = 0xFF5C89F7

internal val MyKSuiteLightColors = MyKSuiteColors(
    primaryTextColor = Color(orca),
    secondaryTextColor = Color(elephant),
    tertiaryTextColor = Color(shark),
    chipBackground = Color(rabbit),
    drive = Color(drive),
    mail = Color(mail),
    driveButton = Color(infomaniak),
    mailButton = Color(mailButton),
)
