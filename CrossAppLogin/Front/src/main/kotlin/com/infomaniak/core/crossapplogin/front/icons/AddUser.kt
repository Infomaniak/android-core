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
package com.infomaniak.core.crossapplogin.front.icons

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

internal val AddUser: ImageVector
    get() {

        if (_addUser != null) return _addUser!!

        _addUser = Builder(
            name = "AddUser",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = EvenOdd,
            ) {
                moveTo(7.239f, 1.021f)
                curveTo(5.265f, 1.021f, 3.665f, 2.622f, 3.665f, 4.596f)
                curveTo(3.665f, 6.57f, 5.265f, 8.171f, 7.239f, 8.171f)
                curveTo(9.214f, 8.171f, 10.814f, 6.57f, 10.814f, 4.596f)
                curveTo(10.814f, 2.622f, 9.214f, 1.021f, 7.239f, 1.021f)
                close()
                moveTo(2.643f, 4.596f)
                curveTo(2.643f, 2.058f, 4.701f, 0.0f, 7.239f, 0.0f)
                curveTo(9.778f, 0.0f, 11.835f, 2.058f, 11.835f, 4.596f)
                curveTo(11.835f, 7.134f, 9.778f, 9.192f, 7.239f, 9.192f)
                curveTo(4.701f, 9.192f, 2.643f, 7.134f, 2.643f, 4.596f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = EvenOdd,
            ) {
                moveTo(7.46f, 10.637f)
                curveTo(6.344f, 10.597f, 5.238f, 10.858f, 4.259f, 11.394f)
                curveTo(3.279f, 11.929f, 2.461f, 12.718f, 1.891f, 13.678f)
                curveTo(1.409f, 14.492f, 1.12f, 15.402f, 1.042f, 16.34f)
                horizontalLineTo(8.201f)
                curveTo(8.483f, 16.34f, 8.711f, 16.569f, 8.711f, 16.851f)
                curveTo(8.711f, 17.133f, 8.483f, 17.362f, 8.201f, 17.362f)
                horizontalLineTo(0.511f)
                curveTo(0.229f, 17.362f, 0.0f, 17.133f, 0.0f, 16.851f)
                curveTo(-0.0f, 15.551f, 0.35f, 14.275f, 1.013f, 13.157f)
                curveTo(1.676f, 12.039f, 2.628f, 11.12f, 3.769f, 10.497f)
                curveTo(4.91f, 9.874f, 6.197f, 9.57f, 7.496f, 9.616f)
                curveTo(8.795f, 9.662f, 10.058f, 10.057f, 11.152f, 10.76f)
                curveTo(11.389f, 10.912f, 11.458f, 11.228f, 11.306f, 11.465f)
                curveTo(11.153f, 11.703f, 10.837f, 11.771f, 10.6f, 11.619f)
                curveTo(9.661f, 11.016f, 8.576f, 10.676f, 7.46f, 10.637f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = EvenOdd,
            ) {
                moveTo(10.213f, 17.106f)
                curveTo(10.213f, 13.299f, 13.299f, 10.213f, 17.106f, 10.213f)
                curveTo(20.914f, 10.213f, 24.0f, 13.299f, 24.0f, 17.106f)
                curveTo(24.0f, 20.914f, 20.914f, 24.0f, 17.106f, 24.0f)
                curveTo(13.299f, 24.0f, 10.213f, 20.914f, 10.213f, 17.106f)
                close()
                moveTo(17.106f, 13.532f)
                curveTo(17.388f, 13.532f, 17.617f, 13.76f, 17.617f, 14.043f)
                verticalLineTo(16.596f)
                horizontalLineTo(20.17f)
                curveTo(20.452f, 16.596f, 20.681f, 16.824f, 20.681f, 17.106f)
                curveTo(20.681f, 17.388f, 20.452f, 17.617f, 20.17f, 17.617f)
                horizontalLineTo(17.617f)
                verticalLineTo(20.17f)
                curveTo(17.617f, 20.452f, 17.388f, 20.681f, 17.106f, 20.681f)
                curveTo(16.824f, 20.681f, 16.596f, 20.452f, 16.596f, 20.17f)
                verticalLineTo(17.617f)
                horizontalLineTo(14.043f)
                curveTo(13.76f, 17.617f, 13.532f, 17.388f, 13.532f, 17.106f)
                curveTo(13.532f, 16.824f, 13.76f, 16.596f, 14.043f, 16.596f)
                horizontalLineTo(16.596f)
                verticalLineTo(14.043f)
                curveTo(16.596f, 13.76f, 16.824f, 13.532f, 17.106f, 13.532f)
                close()
            }
        }.build()

        return _addUser!!
    }

private var _addUser: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    Box {
        Image(
            imageVector = AddUser,
            contentDescription = null,
            modifier = Modifier.size(250.dp),
        )
    }
}
