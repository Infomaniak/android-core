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
package com.infomaniak.core.twofactorauth.front.elements

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntSize
import com.infomaniak.core.twofactorauth.front.R

fun Modifier.lightSourceBehind(
    maxWidth: Dp,
    cardOuterPadding: Dp,
): Modifier = composed {
    val img = ImageBitmap.imageResource(R.drawable.background_light_source)
    drawBehind {
        val h = 192.dp.toPx()
        val paddingPx = cardOuterPadding.toPx()
        val imgSize = Size((size.width * 2 + paddingPx * 2).coerceAtMost(maxWidth.toPx()), height = h)
        drawImage(
            image = img,
            dstOffset = IntOffset(x = 0, (-h / 2f).toInt()),
            dstSize = imgSize.toIntSize()
        )
    }
}
