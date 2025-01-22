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
package com.infomaniak.lib.confetti

/**
 * The source from which confetti will appear. This can be either a line or a point.
 *
 * Please note that the specified source represents the top left corner of the drawn
 * confetti. If you want the confetti to appear from off-screen, you'll have to offset it
 * with the confetti's size.
 *
 * Specifies a line source from which all confetti will emit from.
 *
 * @param x0 x-coordinate of the first point relative to the [ConfettiView]'s parent.
 * @param y0 y-coordinate of the first point relative to the [ConfettiView]'s parent.
 * @param x1 x-coordinate of the second point relative to the [ConfettiView]'s parent.
 * @param y1 y-coordinate of the second point relative to the [ConfettiView]'s parent.
 */
class ConfettiSource(private val x0: Int, private val y0: Int, private val x1: Int, private val y1: Int) {

    /**
     * Specifies a point source from which all confetti will emit from.
     *
     * @param x x-coordinate of the point relative to the [ConfettiView]'s parent.
     * @param y y-coordinate of the point relative to the [ConfettiView]'s parent.
     */
    constructor(x: Int, y: Int) : this(x, y, x, y)

    fun getInitialX(random: Float): Float = x0 + (x1 - x0) * random

    fun getInitialY(random: Float): Float = y0 + (y1 - y0) * random
}
