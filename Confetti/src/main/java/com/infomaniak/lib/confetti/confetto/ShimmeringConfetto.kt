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

import android.animation.ArgbEvaluator
import android.graphics.*
import android.os.SystemClock
import java.util.Random
import kotlin.math.abs

class ShimmeringConfetto(
    bitmap: Bitmap?,
    private val fromColor: Int,
    private val toColor: Int,
    private val waveLength: Long,
    random: Random,
) : BitmapConfetto(bitmap!!) {

    private val evaluator = ArgbEvaluator()
    private val halfWaveLength: Long = waveLength / 2L
    private val randomStart: Long

    init {
        val currentTime = abs(SystemClock.elapsedRealtime().toInt())
        randomStart = (currentTime - random.nextInt(currentTime)).toLong()
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
        val currentTime = SystemClock.elapsedRealtime()
        val fraction = (currentTime - randomStart) % waveLength

        val animated = if (fraction < halfWaveLength) {
            fraction.toFloat() / halfWaveLength
        } else {
            (waveLength.toFloat() - fraction) / halfWaveLength
        }

        val color = evaluator.evaluate(animated, fromColor, toColor) as Int
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)

        super.drawInternal(canvas, matrix, paint, x, y, rotation, percentAnimated)
    }
}
