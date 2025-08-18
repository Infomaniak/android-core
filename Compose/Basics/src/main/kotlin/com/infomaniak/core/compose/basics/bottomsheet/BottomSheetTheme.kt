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
package com.infomaniak.core.compose.basics.bottomsheet

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

class BottomSheetTheme internal constructor(
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    val dragHandleColor: Color = Color.Gray,
    val dragHandleSize: DpSize = DpSize(42.dp, 4.dp),
    val dragHandleShape: Shape = CircleShape,
    val titleTextStyle: TextStyle = TextStyle.Default,
    val titleColor: Color = Color.Unspecified,
)

val LocalBottomSheetTheme = staticCompositionLocalOf { BottomSheetTheme() }

object BottomSheetThemeDefaults {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun theme(
        containerColor: Color = BottomSheetDefaults.ContainerColor,
        contentColor: Color = contentColorFor(containerColor),
        shape: Shape = BottomSheetDefaults.ExpandedShape,
        dragHandleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant, // Default value of BottomSheetDefaults.DragHandle
        dragHandleSize: DpSize = DpSize(42.dp, 4.dp),
        dragHandleShape: Shape = MaterialTheme.shapes.extraLarge, // Default value of BottomSheetDefaults.DragHandle
        titleTextStyle: TextStyle = TextStyle.Default,
        titleColor: Color = Color.Unspecified,
    ): BottomSheetTheme = BottomSheetTheme(
        containerColor = containerColor,
        contentColor = contentColor,
        shape = shape,
        dragHandleColor = dragHandleColor,
        dragHandleSize = dragHandleSize,
        dragHandleShape = dragHandleShape,
        titleTextStyle = titleTextStyle,
        titleColor = titleColor
    )
}

@Composable
fun ProvideBottomSheetTheme(theme: BottomSheetTheme = BottomSheetThemeDefaults.theme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBottomSheetTheme provides theme, content)
}
