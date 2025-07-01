/*
 * Infomaniak SwissTransfer - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.core.crossloginui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Chevron: ImageVector
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
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = EvenOdd,
            ) {
                moveTo(1.651f, 4.643f)
                curveTo(1.849f, 4.451f, 2.166f, 4.455f, 2.358f, 4.653f)
                lineTo(8.0f, 10.455f)
                lineTo(13.642f, 4.653f)
                curveTo(13.834f, 4.455f, 14.151f, 4.451f, 14.349f, 4.643f)
                curveTo(14.547f, 4.836f, 14.551f, 5.152f, 14.358f, 5.35f)
                lineTo(8.641f, 11.229f)
                curveTo(8.641f, 11.229f, 8.641f, 11.23f, 8.641f, 11.23f)
                curveTo(8.641f, 11.23f, 8.641f, 11.23f, 8.641f, 11.23f)
                curveTo(8.289f, 11.592f, 7.711f, 11.592f, 7.359f, 11.23f)
                curveTo(7.359f, 11.23f, 7.359f, 11.23f, 7.359f, 11.23f)
                curveTo(7.359f, 11.23f, 7.359f, 11.229f, 7.359f, 11.229f)
                lineTo(1.642f, 5.35f)
                curveTo(1.449f, 5.152f, 1.453f, 4.836f, 1.651f, 4.643f)
                close()
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
