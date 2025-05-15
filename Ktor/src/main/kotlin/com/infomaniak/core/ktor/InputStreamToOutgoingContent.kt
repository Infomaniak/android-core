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
package com.infomaniak.core.ktor

import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.invoke
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun InputStream.toOutgoingContent(
    length: Long
): OutgoingContent.WriteChannelContent = object : OutgoingContent.WriteChannelContent() {
    override val contentLength = length

    override suspend fun writeTo(channel: ByteWriteChannel) = Dispatchers.IO {
        use { inputStream ->
            inputStream.copyToUntil(channel.toOutputStream(), count = length, coroutineContext = coroutineContext)
        }
    }
}

/**
 * Copies [count] bytes of this [InputStream] to [out].
 *
 * **Note** It is the caller's responsibility to close both of these resources.
 *
 * The implementation is based on the [java.io.InputStream.copyTo] extension from kotlin.io,
 * but also handles cancellation if the [coroutineContext] is passed,
 * in addition to copying only the passed number of bytes in [count].
 */
@Throws(IOException::class)
private fun InputStream.copyToUntil(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    count: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) {
    var bytesRemaining: Long = count
    val buffer = ByteArray(bufferSize)
    do {
        coroutineContext.ensureActive()
        val bytesToCopy = minOf(bytesRemaining, bufferSize.toLong()).toInt()
        val readBytesCount = read(/* b = */ buffer, /* off = */ 0, /* len = */ bytesToCopy)

        if (readBytesCount == 0) {
            throw IOException("Wanted to read $bytesToCopy bytes but got zero. $bytesRemaining/$count bytes couldn't be copied.")
        }
        if (readBytesCount < 0) throw EOFException("$bytesRemaining/$count bytes couldn't be copied.")

        coroutineContext.ensureActive()
        out.write(
            /* b = */ buffer,
            /* off = */ 0,
            /* len = */ readBytesCount
        )
        bytesRemaining -= readBytesCount
    } while (bytesRemaining > 0)
}
