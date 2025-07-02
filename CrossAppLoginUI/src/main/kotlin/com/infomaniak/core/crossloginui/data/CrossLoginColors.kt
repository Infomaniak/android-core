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
    val primaryColor: Color,
    val titleColor: Color,
    val descriptionColor: Color,
    val backgroundColor: Color,
    val buttonStrokeColor: Color,
)

@Composable
fun getCrossLoginColors(
    primaryColor: Int?,
    titleColor: Int?,
    descriptionColor: Int?,
    backgroundColor: Int?,
    buttonStrokeColor: Int?,
): CrossLoginColors {
    return CrossLoginColors(
        primaryColor = primaryColor?.let(::Color) ?: MaterialTheme.colorScheme.primary,
        titleColor = titleColor?.let(::Color) ?: MaterialTheme.colorScheme.secondary,
        descriptionColor = descriptionColor?.let(::Color) ?: MaterialTheme.colorScheme.tertiary,
        backgroundColor = backgroundColor?.let(::Color) ?: MaterialTheme.colorScheme.onPrimary,
        buttonStrokeColor = buttonStrokeColor?.let(::Color) ?: MaterialTheme.colorScheme.outlineVariant,
    )
}
