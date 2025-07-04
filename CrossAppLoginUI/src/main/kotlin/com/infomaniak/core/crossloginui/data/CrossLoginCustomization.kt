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
package com.infomaniak.core.crossloginui.data

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class CrossLoginCustomization(
    val primaryColor: Color, // Only needed in kMail because we are dissociated from the Material theme actual value
    val onPrimaryColor: Color, // Only needed in kMail because we are dissociated from the Material theme actual value
    val titleColor: Color,
    val descriptionColor: Color,
    val avatarStrokeColor: Color,
    val buttonStrokeColor: Color,
    val buttonType: CrossLoginButtonType,
)

enum class CrossLoginButtonType(
    val height: Dp,
    val shape: Shape,
) {
    Mail(height = 56.dp, shape = RoundedCornerShape(16.dp)),
    Drive(height = 70.dp, shape = RoundedCornerShape(10.dp)),
}

internal object CrossLoginDefaults {

    @Composable
    fun customize(
        primaryColor: Color? = null,
        onPrimaryColor: Color? = null,
        titleColor: Color? = null,
        descriptionColor: Color? = null,
        avatarStrokeColor: Color? = null,
        buttonStrokeColor: Color? = null,
        buttonType: CrossLoginButtonType? = null,
    ): CrossLoginCustomization {
        return CrossLoginCustomization(
            primaryColor = primaryColor ?: MaterialTheme.colorScheme.primary,
            onPrimaryColor = onPrimaryColor ?: MaterialTheme.colorScheme.onPrimary,
            titleColor = titleColor ?: MaterialTheme.colorScheme.onSurface,
            descriptionColor = descriptionColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            avatarStrokeColor = avatarStrokeColor ?: MaterialTheme.colorScheme.background,
            buttonStrokeColor = buttonStrokeColor ?: MaterialTheme.colorScheme.outlineVariant,
            buttonType = buttonType ?: CrossLoginButtonType.Mail,
        )
    }
}
