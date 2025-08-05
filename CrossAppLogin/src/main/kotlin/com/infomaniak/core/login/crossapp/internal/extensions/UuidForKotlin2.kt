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
package com.infomaniak.core.login.crossapp.internal.extensions

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val BYTE_TO_LOWER_CASE_HEX_DIGITS_KOTLIN2 = IntArray(256) {
    (LOWER_CASE_HEX_DIGITS_KOTLIN2[(it shr 4)].code shl 8) or LOWER_CASE_HEX_DIGITS_KOTLIN2[(it and 0xF)].code
}

private const val LOWER_CASE_HEX_DIGITS_KOTLIN2 = "0123456789abcdef"

/**
 * Since in some projects we are using Realm Kotlin, we are locked to Kotlin 2.0 and can't bump to Kotlin 2.1.
 * So we extracted the Kotlin 2.1 specific code to be able to use it while we are locked to Kotlin 2.0.
 */
@OptIn(ExperimentalUuidApi::class)
internal fun Uuid.toHexDashStringKotlin2(): String {
    val bytes = ByteArray(36)
    toLongs { mostSignificantBits, leastSignificantBits ->
        mostSignificantBits.formatBytesIntoKotlin2(bytes, 0, startIndex = 0, endIndex = 4)
        bytes[8] = '-'.code.toByte()
        mostSignificantBits.formatBytesIntoKotlin2(bytes, 9, startIndex = 4, endIndex = 6)
        bytes[13] = '-'.code.toByte()
        mostSignificantBits.formatBytesIntoKotlin2(bytes, 14, startIndex = 6, endIndex = 8)
        bytes[18] = '-'.code.toByte()
        leastSignificantBits.formatBytesIntoKotlin2(bytes, 19, startIndex = 0, endIndex = 2)
        bytes[23] = '-'.code.toByte()
        leastSignificantBits.formatBytesIntoKotlin2(bytes, 24, startIndex = 2, endIndex = 8)
    }
    return bytes.decodeToString()
}

private fun Long.formatBytesIntoKotlin2(dst: ByteArray, dstOffset: Int, startIndex: Int, endIndex: Int) {
    var dstIndex = dstOffset
    for (reversedIndex in 7 - startIndex downTo 8 - endIndex) {
        val shift = reversedIndex shl 3
        val byte = ((this shr shift) and 0xFF).toInt()
        val byteDigits = BYTE_TO_LOWER_CASE_HEX_DIGITS_KOTLIN2[byte]
        dst[dstIndex++] = (byteDigits shr 8).toByte()
        dst[dstIndex++] = byteDigits.toByte()
    }
}
