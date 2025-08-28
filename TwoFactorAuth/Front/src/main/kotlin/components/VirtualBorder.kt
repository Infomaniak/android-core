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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun rememberVirtualBorderState(): VirtualBorderState = remember { VirtualBorderStateImpl() }

/**
 * # WARNING: Make sure you put this after any padding to get the desired placement.
 */
fun Modifier.virtualBorderHost(borderState: VirtualBorderState): Modifier = drawWithCache {
    val brush: Brush = SolidColor(Color.Gray)
    val radius = CornerRadius(x = 16.dp.toPx())
    val drawStyle = Stroke(width = 1.dp.toPx())
    onDrawWithContent {
        drawContent()
        drawRoundRect(
            brush = brush,
            cornerRadius = radius,
            style = drawStyle,
            topLeft = borderState.topLeft,
            size = borderState.size.offsetSize(borderState.topLeft)
        )
    }
}

fun Modifier.virtualBorderTopLeftCorner(borderState: VirtualBorderState): Modifier = onPlaced {
    borderState as VirtualBorderStateImpl
    borderState.topLeft = it.positionInParent()
}

fun Modifier.virtualBorderBottomLeftCorner(borderState: VirtualBorderState): Modifier = onPlaced {
    borderState as VirtualBorderStateImpl
    borderState.size = it.positionInParent() + it.size
}

sealed interface VirtualBorderState {
    val topLeft: Offset
    val size: Size
}

private operator fun IntSize.plus(offset: Offset): Size {
    return Size(width = width + offset.x, height = height + offset.y)
}

private operator fun Offset.plus(size: IntSize): Size {
    return Size(width = x + size.width, height = y + size.height)
}

/** Helper method to offset the provided size with the offset in box width and height */
private fun Size.offsetSize(offset: Offset): Size =
    Size(this.width - offset.x, this.height - offset.y)

@Stable
private class VirtualBorderStateImpl : VirtualBorderState {
    private var topLeftValue by mutableLongStateOf(0L)
    private var sizeValue by mutableLongStateOf(0L)

    override var topLeft: Offset
        get() = Offset(packedValue = topLeftValue)
        set(value) {
            topLeftValue = value.packedValue
        }

    override var size: Size
        get() = Size(packedValue = sizeValue)
        set(value) {
            sizeValue = value.packedValue
        }
}
