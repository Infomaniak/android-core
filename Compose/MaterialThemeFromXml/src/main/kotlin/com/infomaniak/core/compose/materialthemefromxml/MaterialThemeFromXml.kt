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
package com.infomaniak.core.compose.materialthemefromxml

import android.content.Context
import androidx.annotation.AttrRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.MaterialColors
import com.google.android.material.R as RMaterial

/**
 * Automatically defines the jetpack compose MaterialTheme based on the values defined as an XML theme in the context where
 * this is called.
 */
@Composable
fun MaterialThemeFromXml(content: @Composable () -> Unit): Unit = with(LocalContext.current) {
    val primary = getMaterialColor(RMaterial.attr.colorPrimary)

    MaterialTheme(
        colorScheme = ColorScheme(
            primary = primary,
            onPrimary = getMaterialColor(RMaterial.attr.colorOnPrimary),
            primaryContainer = getMaterialColor(RMaterial.attr.colorPrimaryContainer),
            onPrimaryContainer = getMaterialColor(RMaterial.attr.colorOnPrimaryContainer),
            inversePrimary = getMaterialColor(RMaterial.attr.colorPrimaryInverse),
            secondary = getMaterialColor(RMaterial.attr.colorSecondary),
            onSecondary = getMaterialColor(RMaterial.attr.colorOnSecondary),
            secondaryContainer = getMaterialColor(RMaterial.attr.colorSecondaryContainer),
            onSecondaryContainer = getMaterialColor(RMaterial.attr.colorOnSecondaryContainer),
            tertiary = getMaterialColor(RMaterial.attr.colorTertiary),
            onTertiary = getMaterialColor(RMaterial.attr.colorOnTertiary),
            tertiaryContainer = getMaterialColor(RMaterial.attr.colorTertiaryContainer),
            onTertiaryContainer = getMaterialColor(RMaterial.attr.colorOnTertiaryContainer),
            background = getMaterialColor(RMaterial.attr.background),
            onBackground = getMaterialColor(RMaterial.attr.colorOnBackground),
            surface = getMaterialColor(RMaterial.attr.colorSurface),
            onSurface = getMaterialColor(RMaterial.attr.colorOnSurface),
            surfaceVariant = getMaterialColor(RMaterial.attr.colorSurfaceVariant),
            onSurfaceVariant = getMaterialColor(RMaterial.attr.colorOnSurfaceVariant),
            // surfaceTint doesn't exist in xml theming, in jetpack compose it automatically gets defaulted to `primary` when you
            // try to override a theme using darkColorScheme or lightColorScheme
            surfaceTint = primary,
            inverseSurface = getMaterialColor(RMaterial.attr.colorSurfaceInverse),
            inverseOnSurface = getMaterialColor(RMaterial.attr.colorOnSurfaceInverse),
            error = getMaterialColor(RMaterial.attr.colorError),
            onError = getMaterialColor(RMaterial.attr.colorOnError),
            errorContainer = getMaterialColor(RMaterial.attr.colorErrorContainer),
            onErrorContainer = getMaterialColor(RMaterial.attr.colorOnErrorContainer),
            outline = getMaterialColor(RMaterial.attr.colorOutline),
            outlineVariant = getMaterialColor(RMaterial.attr.colorOutlineVariant),
            scrim = getMaterialColor(RMaterial.attr.scrimBackground),
            surfaceBright = getMaterialColor(RMaterial.attr.colorSurfaceBright),
            surfaceContainer = getMaterialColor(RMaterial.attr.colorSurfaceContainer),
            surfaceContainerHigh = getMaterialColor(RMaterial.attr.colorSurfaceContainerHigh),
            surfaceContainerHighest = getMaterialColor(RMaterial.attr.colorSurfaceContainerHighest),
            surfaceContainerLow = getMaterialColor(RMaterial.attr.colorSurfaceContainerLow),
            surfaceContainerLowest = getMaterialColor(RMaterial.attr.colorSurfaceContainerLowest),
            surfaceDim = getMaterialColor(RMaterial.attr.colorSurfaceDim),
        ),
        content = content,
    )
}

private fun Context.getMaterialColor(@AttrRes colorId: Int) = Color(MaterialColors.getColor(this, colorId, 0))
