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
package com.infomaniak.core.inappstore.ui.theme

import android.content.Context
import androidx.annotation.AttrRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.MaterialColors
import com.google.android.material.R as RMaterial

internal val LocalInAppStoreColors: ProvidableCompositionLocal<InAppStoreColors> = staticCompositionLocalOf { InAppStoreColors() }

/**
 * This theme should be used in any XML View's wrapper class, as it uses the XML material attributes
 */
@Composable
internal fun InAppStoreXMLTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current

    InAppStoreTheme(
        primaryColor = getMaterialColor(context, RMaterial.attr.colorPrimary),
        onPrimaryColor = getMaterialColor(context, RMaterial.attr.colorOnPrimary),
        content = content,
    )
}

/**
 * Basic theme to use if the components are called from Compose
 */
@Composable
internal fun InAppStoreTheme(
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onPrimaryColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable () -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val customColors = if (isDarkTheme) InAppStoreDarkColors else InAppStoreLightColors

    CompositionLocalProvider(
        LocalTextStyle provides Typography.bodyRegular,
        LocalInAppStoreColors provides customColors,
    ) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) {
                getDarkColorScheme(primaryColor, onPrimaryColor)
            } else {
                getLightColorScheme(primaryColor, onPrimaryColor)
            },
            content = content,
        )
    }
}

@Immutable
internal data class InAppStoreColors(
    val primaryTextColor: Color = Color.Unspecified,
    val secondaryTextColor: Color = Color.Unspecified,
    val tertiaryTextColor: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val secondaryBackground: Color = Color.Unspecified,
    val topAppBarBackground: Color = Color.Unspecified,
    val informationBlockBackground: Color = Color.Unspecified,
    val chipBackground: Color = Color.Unspecified,
    val drive: Color = Color.Unspecified,
    val mail: Color = Color.Unspecified,
    val primaryButton: Color = Color.Unspecified,
    val onPrimaryButton: Color = Color.Unspecified,
    val mailButton: Color = Color.Unspecified,
    val onMailButton: Color = Color.Unspecified,
    val iconColor: Color = Color.Unspecified,
    val cardBorderColor: Color = Color.Unspecified,
)

private fun getMaterialColor(context: Context, @AttrRes colorId: Int) = Color(MaterialColors.getColor(context, colorId, 0))
