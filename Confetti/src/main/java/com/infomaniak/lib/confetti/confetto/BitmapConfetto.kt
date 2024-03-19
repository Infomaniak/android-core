/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2024 Infomaniak Network SA
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
package com.infomaniak.lib.confetti.confetto

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

open class BitmapConfetto(private val bitmap: Bitmap) : Confetto() {

    private val bitmapCenterX: Float = bitmap.width / 2.0f
    private val bitmapCenterY: Float = bitmap.height / 2.0f

    override val width: Int get() = bitmap.width

    override val height: Int get() = bitmap.height

    override fun drawInternal(
        canvas: Canvas,
        matrix: Matrix,
        paint: Paint,
        x: Float,
        y: Float,
        rotation: Float,
        percentAnimated: Float,
    ) {
        matrix.preTranslate(x, y)
        matrix.preRotate(rotation, bitmapCenterX, bitmapCenterY)
        canvas.drawBitmap(bitmap, matrix, paint)
    }
}
