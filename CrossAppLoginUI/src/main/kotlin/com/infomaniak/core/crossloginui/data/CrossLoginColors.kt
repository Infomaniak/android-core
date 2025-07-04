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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class CrossLoginColors(
    val primary: Color, // Only needed in kMail because we are dissociated from the Material theme actual value
    val onPrimary: Color, // Only needed in kMail because we are dissociated from the Material theme actual value
    val title: Color,
    val description: Color,
    val avatarStroke: Color,
    val buttonStroke: Color,
)

internal object CrossLoginDefaults {

    @Composable
    fun colors(
        primary: Color? = null,
        onPrimary: Color? = null,
        title: Color? = null,
        description: Color? = null,
        avatarStroke: Color? = null,
        buttonStroke: Color? = null,
    ): CrossLoginColors {
        return CrossLoginColors(
            primary = primary ?: MaterialTheme.colorScheme.primary,
            onPrimary = onPrimary ?: MaterialTheme.colorScheme.onPrimary,
            title = title ?: MaterialTheme.colorScheme.onSurface,
            description = description ?: MaterialTheme.colorScheme.onSurfaceVariant,
            avatarStroke = avatarStroke ?: MaterialTheme.colorScheme.background,
            buttonStroke = buttonStroke ?: MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
