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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

enum class CardKind {
    Outlined,
    Elevated,
    Normal,
}

@Composable
fun rememberVirtualCardState(kind: CardKind): VirtualCardState = rememberVirtualCardState(
    shape = when (kind) {
        CardKind.Outlined -> CardDefaults.outlinedShape
        CardKind.Normal -> CardDefaults.shape
        CardKind.Elevated -> CardDefaults.elevatedShape
    },
    colors = when (kind) {
        CardKind.Outlined -> CardDefaults.outlinedCardColors()
        CardKind.Elevated -> CardDefaults.elevatedCardColors()
        CardKind.Normal -> CardDefaults.cardColors()
    },
    elevation = when (kind) {
        CardKind.Outlined -> CardDefaults.outlinedCardElevation()
        CardKind.Elevated -> CardDefaults.elevatedCardElevation()
        CardKind.Normal -> CardDefaults.cardElevation()
    },
    border = when (kind) {
        CardKind.Outlined -> CardDefaults.outlinedCardBorder()
        else -> null
    }
)

@Composable
fun rememberVirtualCardState(
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    elevation: CardElevation = CardDefaults.outlinedCardElevation(),
    border: BorderStroke?
): VirtualCardState {
    val colorsState = rememberUpdatedState(colors)
    val elevationState = rememberUpdatedState(elevation)
    val borderState = rememberUpdatedState(border)
    return remember(shape) {
        VirtualCardStateImpl(shape, colorsState, elevationState, borderState)
    }
}

/**
 * # WARNING: Make sure you put this after any padding to get the desired placement.
 */
fun Modifier.virtualCardHost(borderState: VirtualCardState): Modifier = drawWithCache {
    val border = borderState.mutable.borderState.value ?: return@drawWithCache onDrawBehind {}
    if (borderState.size.isEmpty() || borderState.topLeft.isUnspecified) return@drawWithCache onDrawBehind {}

    val drawStyle = Stroke(width = border.width.toPx())
    val outline = borderState.mutable.shape.createOutline(
        size = borderState.size.offsetSize(borderState.topLeft),
        layoutDirection = layoutDirection,
        density = this
    )
    onDrawWithContent {
        drawContent()
        translate(left = borderState.topLeft.x, top = borderState.topLeft.y) {
            drawOutline(
                outline = outline,
                brush = border.brush,
                style = drawStyle
            )
        }
    }
}

enum class CardElementPosition {
    First,
    Middle,
    Last,
}

@Composable
fun CardElement(
    virtualCardState: VirtualCardState,
    modifier: Modifier = Modifier,
    elementPosition: CardElementPosition = CardElementPosition.Middle,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val shape = when (elementPosition) {
        CardElementPosition.First -> virtualCardState.firstElementShape
        CardElementPosition.Middle -> virtualCardState.middleElementsShape
        CardElementPosition.Last -> virtualCardState.lastElementShape
    }
    Card(
        modifier = when (elementPosition) {
            CardElementPosition.First -> modifier.virtualBorderTopLeftCorner(virtualCardState)
            CardElementPosition.Middle -> modifier
            CardElementPosition.Last -> modifier.virtualBorderBottomLeftCorner(virtualCardState)
        },
        shape = shape,
        colors = virtualCardState.colors,
        elevation = virtualCardState.elevation,
        content = content
    )
}

fun Modifier.virtualBorderTopLeftCorner(borderState: VirtualCardState): Modifier = composed {
    DisposableEffect(Unit) { onDispose { borderState.mutable.topLeft = Offset.Unspecified } }
    onPlaced { borderState.mutable.topLeft = it.positionInParent() }
}

fun Modifier.virtualBorderBottomLeftCorner(borderState: VirtualCardState): Modifier = composed {
    DisposableEffect(Unit) { onDispose { borderState.mutable.size = Size.Zero } }
    onPlaced { borderState.mutable.size = it.positionInParent() + it.size }
}

sealed interface VirtualCardState {
    val firstElementShape: Shape
    val lastElementShape: Shape
    val middleElementsShape: Shape

    val colors: CardColors
    val elevation: CardElevation

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

private val VirtualCardState.mutable: VirtualCardStateImpl
    get() = when (this) {
        is VirtualCardStateImpl -> this
    }

@Stable
private class VirtualCardStateImpl(
    val shape: Shape,
    colorsState: State<CardColors>,
    elevationState: State<CardElevation>,
    val borderState: State<BorderStroke?>,
) : VirtualCardState {

    override val firstElementShape: Shape = (shape as? CornerBasedShape)?.removeCorners(
        keepTopCorners = true,
        keepBottomCorners = false
    ) ?: shape

    override val lastElementShape = (shape as? CornerBasedShape)?.removeCorners(
        keepTopCorners = false,
        keepBottomCorners = true
    ) ?: shape

    override val middleElementsShape = RectangleShape

    override val colors: CardColors by colorsState
    override val elevation: CardElevation by elevationState
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

private fun CornerBasedShape.removeCorners(
    keepTopCorners: Boolean,
    keepBottomCorners: Boolean
): CornerBasedShape = copy(
    topStart = if (keepTopCorners) topStart else zeroCorner,
    topEnd = if (keepTopCorners) topEnd else zeroCorner,
    bottomStart = if (keepBottomCorners) bottomStart else zeroCorner,
    bottomEnd = if (keepBottomCorners) bottomEnd else zeroCorner,
)

private val zeroCorner = CornerSize(0.dp)
