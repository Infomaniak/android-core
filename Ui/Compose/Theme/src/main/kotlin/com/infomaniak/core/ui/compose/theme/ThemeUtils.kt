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
package com.infomaniak.core.ui.compose.theme

import android.app.UiModeManager
import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.android.material.color.ColorContrast.isContrastAvailable

@Composable
fun selectSchemeForContrast(
    isDark: Boolean,
    darkScheme: ColorScheme,
    lightScheme: ColorScheme,
    mediumContrastDarkColorScheme: ColorScheme,
    mediumContrastLightColorScheme: ColorScheme,
    highContrastDarkColorScheme: ColorScheme,
    highContrastLightColorScheme: ColorScheme,
): ColorScheme {
    fun defaultColorScheme(): ColorScheme = if (isDark) darkScheme else lightScheme

    val isPreview = LocalInspectionMode.current
    return if (!isPreview && isContrastAvailable()) {
        val uiModeManager = LocalContext.current.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val contrastLevel = uiModeManager.contrast

        when (contrastLevel) {
            in 0.0f..0.33f -> defaultColorScheme()
            in 0.33f..0.66f -> if (isDark) mediumContrastDarkColorScheme else mediumContrastLightColorScheme
            in 0.66f..1.0f -> if (isDark) highContrastDarkColorScheme else highContrastLightColorScheme
            else -> defaultColorScheme()
        }
    } else defaultColorScheme()
}
