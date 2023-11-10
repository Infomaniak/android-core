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
package com.infomaniak.lib.confetti.confetto

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

/**
 * A lightly more optimal way to draw a circle shape that doesn't require the use of a bitmap.
 */
class CircleConfetto(private val color: Int, private val radius: Float) : Confetto() {

    private val diameter: Int = (radius * 2.0f).toInt()

    override val width: Int get() = diameter

    override val height: Int get() = diameter

    override fun configurePaint(paint: Paint) {
        super.configurePaint(paint)
        paint.style = Paint.Style.FILL
        paint.color = color
    }

    override fun drawInternal(
        canvas: Canvas,
        matrix: Matrix,
        paint: Paint,
        x: Float,
        y: Float,
        rotation: Float,
        percentAnimated: Float,
    ) {
        canvas.drawCircle(x + radius, y + radius, radius, paint)
    }
}
