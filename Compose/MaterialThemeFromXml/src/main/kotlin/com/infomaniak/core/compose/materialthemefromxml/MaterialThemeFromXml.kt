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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
fun MaterialThemeFromXml(content: @Composable () -> Unit) {
    val context = LocalContext.current

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) {
            darkColorScheme(
                primary = getMaterialColor(context, RMaterial.attr.colorPrimary),
                onPrimary = getMaterialColor(context, RMaterial.attr.colorOnPrimary),
                primaryContainer = getMaterialColor(context, RMaterial.attr.colorPrimaryContainer),
                onPrimaryContainer = getMaterialColor(context, RMaterial.attr.colorOnPrimaryContainer),
                inversePrimary = getMaterialColor(context, RMaterial.attr.colorPrimaryInverse),
                secondary = getMaterialColor(context, RMaterial.attr.colorSecondary),
                onSecondary = getMaterialColor(context, RMaterial.attr.colorOnSecondary),
                secondaryContainer = getMaterialColor(context, RMaterial.attr.colorSecondaryContainer),
                onSecondaryContainer = getMaterialColor(context, RMaterial.attr.colorOnSecondaryContainer),
                tertiary = getMaterialColor(context, RMaterial.attr.colorTertiary),
                onTertiary = getMaterialColor(context, RMaterial.attr.colorOnTertiary),
                tertiaryContainer = getMaterialColor(context, RMaterial.attr.colorTertiaryContainer),
                onTertiaryContainer = getMaterialColor(context, RMaterial.attr.colorOnTertiaryContainer),
                background = getMaterialColor(context, RMaterial.attr.background),
                onBackground = getMaterialColor(context, RMaterial.attr.colorOnBackground),
                surface = getMaterialColor(context, RMaterial.attr.colorSurface),
                onSurface = getMaterialColor(context, RMaterial.attr.colorOnSurface),
                surfaceVariant = getMaterialColor(context, RMaterial.attr.colorSurfaceVariant),
                onSurfaceVariant = getMaterialColor(context, RMaterial.attr.colorOnSurfaceVariant),
                // surfaceTint doesn't exist in xml theming, in jetpack compose it automatically gets defaulted to `primary`
                inverseSurface = getMaterialColor(context, RMaterial.attr.colorSurfaceInverse),
                inverseOnSurface = getMaterialColor(context, RMaterial.attr.colorOnSurfaceInverse),
                error = getMaterialColor(context, RMaterial.attr.colorError),
                onError = getMaterialColor(context, RMaterial.attr.colorOnError),
                errorContainer = getMaterialColor(context, RMaterial.attr.colorErrorContainer),
                onErrorContainer = getMaterialColor(context, RMaterial.attr.colorOnErrorContainer),
                outline = getMaterialColor(context, RMaterial.attr.colorOutline),
                outlineVariant = getMaterialColor(context, RMaterial.attr.colorOutlineVariant),
                scrim = getMaterialColor(context, RMaterial.attr.scrimBackground),
                surfaceBright = getMaterialColor(context, RMaterial.attr.colorSurfaceBright),
                surfaceContainer = getMaterialColor(context, RMaterial.attr.colorSurfaceContainer),
                surfaceContainerHigh = getMaterialColor(context, RMaterial.attr.colorSurfaceContainerHigh),
                surfaceContainerHighest = getMaterialColor(context, RMaterial.attr.colorSurfaceContainerHighest),
                surfaceContainerLow = getMaterialColor(context, RMaterial.attr.colorSurfaceContainerLow),
                surfaceContainerLowest = getMaterialColor(context, RMaterial.attr.colorSurfaceContainerLowest),
                surfaceDim = getMaterialColor(context, RMaterial.attr.colorSurfaceDim),
            )
        } else {
            lightColorScheme(
                primary = getMaterialColor(context, RMaterial.attr.colorPrimary),
                onPrimary = getMaterialColor(context, RMaterial.attr.colorOnPrimary),
                primaryContainer = getMaterialColor(context, RMaterial.attr.colorPrimaryContainer),
                onPrimaryContainer = getMaterialColor(context, RMaterial.attr.colorOnPrimaryContainer),
                inversePrimary = getMaterialColor(context, RMaterial.attr.colorPrimaryInverse),
                secondary = getMaterialColor(context, RMaterial.attr.colorSecondary),
                onSecondary = getMaterialColor(context, RMaterial.attr.colorOnSecondary),
                secondaryContainer = getMaterialColor(context, RMaterial.attr.colorSecondaryContainer),
                onSecondaryContainer = getMaterialColor(context, RMaterial.attr.colorOnSecondaryContainer),
                tertiary = getMaterialColor(context, RMaterial.attr.colorTertiary),
                onTertiary = getMaterialColor(context, RMaterial.attr.colorOnTertiary),
                tertiaryContainer = getMaterialColor(context, RMaterial.attr.colorTertiaryContainer),
                onTertiaryContainer = getMaterialColor(context, RMaterial.attr.colorOnTertiaryContainer),
                background = getMaterialColor(context, RMaterial.attr.background),
                onBackground = getMaterialColor(context, RMaterial.attr.colorOnBackground),
                surface = getMaterialColor(context, RMaterial.attr.colorSurface),
                onSurface = getMaterialColor(context, RMaterial.attr.colorOnSurface),
                surfaceVariant = getMaterialColor(context, RMaterial.attr.colorSurfaceVariant),
                onSurfaceVariant = getMaterialColor(context, RMaterial.attr.colorOnSurfaceVariant),
                // surfaceTint doesn't exist in xml theming, in jetpack compose it automatically gets defaulted to `primary`
                inverseSurface = getMaterialColor(context, RMaterial.attr.colorSurfaceInverse),
                inverseOnSurface = getMaterialColor(context, RMaterial.attr.colorOnSurfaceInverse),
                error = getMaterialColor(context, RMaterial.attr.colorError),
                onError = getMaterialColor(context, RMaterial.attr.colorOnError),
                errorContainer = getMaterialColor(context, RMaterial.attr.colorErrorContainer),
                onErrorContainer = getMaterialColor(context, RMaterial.attr.colorOnErrorContainer),
                outline = getMaterialColor(context, RMaterial.attr.colorOutline),
                outlineVariant = getMaterialColor(context, RMaterial.attr.colorOutlineVariant),
                scrim = getMaterialColor(context, RMaterial.attr.scrimBackground),
                surfaceBright = getMaterialColor(context, RMaterial.attr.colorSurfaceBright),
                surfaceContainer = getMaterialColor(context, RMaterial.attr.colorSurfaceContainer),
                surfaceContainerHigh = getMaterialColor(context, RMaterial.attr.colorSurfaceContainerHigh),
                surfaceContainerHighest = getMaterialColor(context, RMaterial.attr.colorSurfaceContainerHighest),
                surfaceContainerLow = getMaterialColor(context, RMaterial.attr.colorSurfaceContainerLow),
                surfaceContainerLowest = getMaterialColor(context, RMaterial.attr.colorSurfaceContainerLowest),
                surfaceDim = getMaterialColor(context, RMaterial.attr.colorSurfaceDim),
            )
        },
        content = content,
    )
}

private fun getMaterialColor(context: Context, @AttrRes colorId: Int) = Color(MaterialColors.getColor(context, colorId, 0))
