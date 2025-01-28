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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

internal val LocalMyKSuiteColors: ProvidableCompositionLocal<MyKSuiteColors> = staticCompositionLocalOf { MyKSuiteColors() }

@Composable
internal fun MyKSuiteTheme(
    content: @Composable () -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val customColors = if (isDarkTheme) MyKSuiteDarkColors else MyKSuiteLightColors

    CompositionLocalProvider(
        LocalTextStyle provides Typography.bodyRegular,
        LocalMyKSuiteColors provides customColors,
    ) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme,
            content = content,
        )
    }
}

// TODO Remove this object and use Directly Typography and Color
internal object MyKSuiteTheme {
    val typography = Typography
    val colors: MyKSuiteColors
        @Composable
        get() = LocalMyKSuiteColors.current
}

@Immutable
internal data class MyKSuiteColors(
    val primaryTextColor: Color = Color.Unspecified,
    val secondaryTextColor: Color = Color.Unspecified,
    val tertiaryTextColor: Color = Color.Unspecified,
    val chipBackground: Color = Color.Unspecified,
    val drive: Color = Color.Unspecified,
    val mail: Color = Color.Unspecified,
    val driveButton: Color = Color.Unspecified,
    val onDriveButton: Color = Color.Unspecified,
    val mailButton: Color = Color.Unspecified,
    val onMailButton: Color = Color.Unspecified,
    val iconColor: Color = Color.Unspecified,
    val cardBorderColor: Color = Color.Unspecified,
)
