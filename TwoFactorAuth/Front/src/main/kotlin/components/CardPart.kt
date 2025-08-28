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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * For border, see VirtualBorder
 */
@Composable
fun CardPart(
    modifier: Modifier = Modifier,
    topCorners: Boolean = false,
    bottomCorners: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val cardShape = when {
        topCorners && bottomCorners -> CardDefaults.shape
        else -> CardDefaults.shape.let {
            (it as? CornerBasedShape)?.removeCorners(
                keepTopCorners = topCorners,
                keepBottomCorners = bottomCorners
            ) ?: it
        }
    }
    Card(
        modifier = modifier,
        shape = cardShape,
        content = content
    )
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
