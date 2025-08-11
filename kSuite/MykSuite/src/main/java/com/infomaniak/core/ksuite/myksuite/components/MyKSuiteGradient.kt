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
package com.infomaniak.core.ksuite.myksuite.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal fun myKSuiteGradient() = BorderStroke(
    width = 1.dp,
    brush = Brush.linearGradient(
        0.0f to Color(0xFF1DDDFD),
        0.3f to Color(0xFF337CFF),
        0.5f to Color(0xFFA055FC),
        0.7f to Color(0xFFF34BBB),
        1.0f to Color(0xFFFD8C3D),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, 0.0f),
    ),
)
