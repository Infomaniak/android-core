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
package com.infomaniak.core.io

import kotlinx.coroutines.ensureActive
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * **DISCLAIMER: Code copied from the JDK, might not follow all our coding conventions.**
 *
 * The implementation is copied from [InputStream.skipNBytes] that is only available starting API 34,
 * but also supports cancellation if the [coroutineContext] is passed.
 */
@Throws(IOException::class)
fun InputStream.skipExactly(
    numberOfBytes: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) {
    var n = numberOfBytes
    while (n > 0) {
        coroutineContext.ensureActive()
        val ns: Long = skip(n)
        if (ns > 0 && ns <= n) {
            // adjust number to skip
            n -= ns
        } else if (ns == 0L) { // no bytes skipped
            // read one byte to check for EOS
            if (read() == -1) {
                throw EOFException()
            }
            // one byte read so decrement number to skip
            n--
        } else { // skipped negative or too many bytes
            throw IOException("Unable to skip exactly")
        }
    }
}
