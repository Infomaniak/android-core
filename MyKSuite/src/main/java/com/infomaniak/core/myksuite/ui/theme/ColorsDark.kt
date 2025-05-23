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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette
private const val bat = 0xFF1A1A1A
private const val orca = 0xFF333333
private const val elephant = 0xFF666666
private const val shark = 0xFF9F9F9F
private const val rabbit = 0xFFF1F1F1

// KSuite App colors
private const val infomaniak = 0xFF4CB7FF
private const val mailButton = 0xFFEF8BA4
private const val onMailButton = 0xFF3F0018
private const val mail = 0xFFF2357A
private const val drive = 0xFF5C89F7

internal val overriddenDarkColorScheme
    @Composable get() = MaterialTheme.colorScheme.copy(
        onSurfaceVariant = Color(rabbit), // Used for bottom sheet drag handle
        surfaceContainerLow = Color(bat), // Used for bottom sheet backgrounds
        surfaceContainerHighest = Color(bat), // Used for Card backgrounds
        outlineVariant = Color(elephant), // Used for divider's color
    )

internal val MyKSuiteDarkColors = MyKSuiteColors(
    primaryTextColor = Color(rabbit),
    secondaryTextColor = Color(shark),
    tertiaryTextColor = Color(elephant),
    background = Color(bat),
    secondaryBackground = Color(orca),
    topAppBarBackground = Color(orca),
    informationBlockBackground = Color(orca),
    chipBackground = Color(orca),
    drive = Color(drive),
    mail = Color(mail),
    primaryButton = Color(infomaniak),
    onPrimaryButton = Color(bat),
    mailButton = Color(mailButton),
    onMailButton = Color(onMailButton),
    iconColor = Color(rabbit),
    cardBorderColor = Color(elephant),
)
