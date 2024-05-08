/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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
package com.infomaniak.lib.core.utils

import android.content.Context
import android.text.format.Formatter
import kotlin.math.abs

object FormatterFileSize {

    private const val KIBI_BYTE = 1_024L
    private const val KILO_BYTE = 1_000L

    private const val FLAG_IEC_UNITS = 1 shl 3
    private const val FLAG_SI_UNITS = 1 shl 2
    private const val FLAG_SHORTER = 1 shl 0

    fun Context.formatShortFileSize(bytes: Long, valueOnly: Boolean = false): String = runCatching {
        val (value, unit) = formatFileSize(bytes, FLAG_IEC_UNITS or FLAG_SHORTER, valueOnly)
        if (unit == null) {
            value
        } else {
            getString(resources.getIdentifier("fileSizeSuffix", "string", "android"), value, unit)
        }
    }.getOrElse {
        Formatter.formatShortFileSize(this, bytes)
    }

    private fun Context.formatFileSize(bytes: Long, flags: Int, valueOnly: Boolean): Pair<String, String?> {

        fun getSuffix(suffixes: MutableList<String>): Int? {
            return if (valueOnly) null else resources.getIdentifier(suffixes.removeFirstOrNull(), "string", "android")
        }

        val suffixes = mutableListOf(
            "byteShort",
            "kilobyteShort",
            "megabyteShort",
            "gigabyteShort",
            "terabyteShort",
            "petabyteShort",
        )
        val unit = if (flags and FLAG_IEC_UNITS != 0) KIBI_BYTE else KILO_BYTE
        var multiplier = 1L

        var result = abs(bytes).toFloat()
        var suffix = getSuffix(suffixes)
        val suffixesCount = suffixes.count()

        repeat(suffixesCount) {
            if (result > 900) {
                suffix = getSuffix(suffixes)
                multiplier *= unit
                result /= unit
            }
        }

        val roundFormat = when {
            multiplier == 1L || result >= 100 -> "%.0f"
            result < 1 -> "%.2f"
            result < 10 -> if (flags and FLAG_SHORTER != 0) "%.1f" else "%.2f"
            else -> if (flags and FLAG_SHORTER != 0) "%.0f" else "%.2f" // 10 <= result < 100
        }

        result = abs(result)
        val resultValue = String.format(roundFormat, result)
        val resultUnit = suffix?.let(resources::getString)

        return resultValue to resultUnit
    }
}
