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
package com.infomaniak.core.twofactorauth.front.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SecurityTheme(
    isInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = MaterialTheme(
    colorScheme = when {
        isInDarkTheme -> darkColorScheme(
            primary = Color(0xFF_5869D9),
            onPrimary = Color(0xFF_EAECFA),
            secondary = Color.Red,
            secondaryContainer = Color(0xFF_ABB4EC),
            onSecondaryContainer = Color(0xFF_121B53),
            surface = Color(0xFF_191919),
            surfaceContainerLow = Color(0xFF_191919),
            surfaceContainerHighest = Color(0xFF_191919),
        )
        else -> lightColorScheme(
            primary = Color(0xFF_3243AE),
            onPrimary = Color(0xFF_EAECFA),
            secondaryContainer = Color(0xFF_D5D9F5),
            onSecondaryContainer = Color(0xFF_121B53),
            surface = Color(0xFF_F4F6FD),
            surfaceContainerLow = Color.White,
            surfaceContainer = Color.White,
            surfaceContainerHighest = Color.White,
        )
    },
    content = content
)
