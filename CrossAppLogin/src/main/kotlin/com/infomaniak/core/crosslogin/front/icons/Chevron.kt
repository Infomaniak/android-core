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
package com.infomaniak.core.crosslogin.front.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.StrokeCap.Companion.Round as strokeCapRound
import androidx.compose.ui.graphics.StrokeJoin.Companion.Round as strokeJoinRound

internal val Chevron: ImageVector
    get() {

        if (_chevron != null) return _chevron!!

        _chevron = Builder(
            name = "Chevron",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF9F9F9F)),
                strokeLineWidth = 2.0f,
                strokeLineCap = strokeCapRound,
                strokeLineJoin = strokeJoinRound,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero,
            ) {
                moveTo(15.0f, 5.0f)
                lineTo(8.33f, 11.859f)
                curveTo(8.286f, 11.904f, 8.235f, 11.939f, 8.179f, 11.964f)
                curveTo(8.122f, 11.988f, 8.061f, 12.0f, 8.0f, 12.0f)
                curveTo(7.939f, 12.0f, 7.878f, 11.988f, 7.821f, 11.964f)
                curveTo(7.765f, 11.939f, 7.714f, 11.904f, 7.67f, 11.859f)
                lineTo(1.0f, 5.0f)
            }
        }.build()

        return _chevron!!
    }

private var _chevron: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    Box {
        Image(
            imageVector = Chevron,
            contentDescription = null,
            modifier = Modifier.size(250.dp),
        )
    }
}
