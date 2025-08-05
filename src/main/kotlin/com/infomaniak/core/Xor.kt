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
package com.infomaniak.core

/**
 * This class is a substitute for union types (that will exist as "error types" in Kotlin 2.3+).
 *
 * It is similar to `Either` from Arrow, but with fewer features (because some are not needed).
 *
 * XOR stands for eXclusive OR.
 */
sealed class Xor<out FirstT, out SecondT> {

    fun firstOrNull(): FirstT? = if (this is First) this.value else null
    fun secondOrNull(): SecondT? = if (this is Second) this.value else null

    data class First<LeftT>(val value: LeftT) : Xor<LeftT, Nothing>()

    data class Second<RightT>(val value: RightT) : Xor<Nothing, RightT>()
}
