/*
 * Infomaniak Core - Android
 * Copyright (C) 2023 Infomaniak Network SA
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
package com.infomaniak.lib.confetti

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.animation.Interpolator
import kotlin.math.tan

object Utils {

    private val paint = Paint().apply { style = Paint.Style.FILL }

    @JvmStatic
    var defaultAlphaInterpolator: Interpolator? = null
        get() {
            if (field == null) field = Interpolator { v: Float -> if (v >= 0.9f) 1.0f - (v - 0.9f) * 10.0f else 1.0f }
            return field
        }
        private set

    @JvmStatic
    fun generateConfettiBitmaps(colors: IntArray, size: Int): List<Bitmap> = mutableListOf<Bitmap>().apply {
        for (color in colors) {
            add(createCircleBitmap(color, size))
            add(createSquareBitmap(color, size))
            add(createTriangleBitmap(color, size))
        }
    }

    fun createCircleBitmap(color: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = size / 2.0f

        paint.color = color
        canvas.drawCircle(radius, radius, radius, paint)

        return bitmap
    }

    fun createSquareBitmap(color: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path = Path().apply {
            moveTo(0.0f, 0.0f)
            lineTo(size.toFloat(), 0.0f)
            lineTo(size.toFloat(), size.toFloat())
            lineTo(0.0f, size.toFloat())
            close()
        }

        paint.color = color
        canvas.drawPath(path, paint)

        return bitmap
    }

    fun createTriangleBitmap(color: Int, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Generate equilateral triangle (http://mathworld.wolfram.com/EquilateralTriangle.html).
        val path = Path().apply {
            val point = tan(15.0f / 180.0f * Math.PI).toFloat() * size
            moveTo(0.0f, 0.0f)
            lineTo(size.toFloat(), point)
            lineTo(point, size.toFloat())
            close()
        }

        paint.color = color
        canvas.drawPath(path, paint)

        return bitmap
    }
}
