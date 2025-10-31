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
package com.infomaniak.core.crossapplogin.front.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.infomaniak.core.compose.basics.ButtonStyle
import com.infomaniak.core.compose.basics.ButtonType

data class CrossLoginCustomization(
    val colors: CrossLoginColors,
    val buttonStyle: ButtonStyle,
)

data class CrossLoginColors(
    val primaryColor: Color, // Only needed in kMail because we are dissociated from the Material theme actual value
    val onPrimaryColor: Color, // Only needed in kMail because we are dissociated from the Material theme actual value
    val titleColor: Color,
    val descriptionColor: Color,
    val avatarStrokeColor: Color,
    val buttonStrokeColor: Color,
)

object CrossLoginDefaults {

    @Composable
    fun customize(
        colors: CrossLoginColors = colors(),
        buttonStyle: ButtonStyle = buttonType(),
    ): CrossLoginCustomization = CrossLoginCustomization(colors, buttonStyle)

    @Composable
    fun colors(
        primaryColor: Color? = null,
        onPrimaryColor: Color? = null,
        titleColor: Color? = null,
        descriptionColor: Color? = null,
        avatarStrokeColor: Color? = null,
        buttonStrokeColor: Color? = null,
    ): CrossLoginColors = CrossLoginColors(
        primaryColor = primaryColor ?: MaterialTheme.colorScheme.primary,
        onPrimaryColor = onPrimaryColor ?: MaterialTheme.colorScheme.onPrimary,
        titleColor = titleColor ?: MaterialTheme.colorScheme.onSurface,
        descriptionColor = descriptionColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
        avatarStrokeColor = avatarStrokeColor ?: MaterialTheme.colorScheme.surface,
        buttonStrokeColor = buttonStrokeColor ?: MaterialTheme.colorScheme.outlineVariant,
    )

    fun buttonType(buttonType: ButtonStyle? = null): ButtonStyle {
        return buttonType ?: ButtonType.Mail
    }
}
