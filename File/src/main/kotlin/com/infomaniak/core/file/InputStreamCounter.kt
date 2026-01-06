/*
 * Infomaniak SwissTransfer - Android
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
package com.infomaniak.core.file

import android.net.Uri
import splitties.init.appCtx
import java.io.InputStream
import kotlin.io.buffered
import kotlin.io.use

internal class InputStreamCounter(
    bufferSize: Int = DEFAULT_BUFFER_SIZE // 8kiB only
) {

    private val sharedByteArrayForCountingBytes = ByteArray(bufferSize)

    inline fun fileSizeFor(
        uri: Uri,
        onChunkRead: (countedBytes: Int, totalBytes: Long) -> Unit,
    ): Long? {
        return fileSizeFor(
            stream = appCtx.contentResolver.openInputStream(uri)?.buffered(sharedByteArrayForCountingBytes.size) ?: return null,
            onChunkRead = onChunkRead,
        )
    }

    /**
     * [android.provider.OpenableColumns.SIZE] is declarative, and could therefore have an inaccurate value,
     * so we're counting the bytes from the stream instead.
     */
    inline fun fileSizeFor(
        stream: InputStream,
        onChunkRead: (countedBytes: Int, totalBytes: Long) -> Unit,
    ): Long = stream.use { stream ->
        var totalBytes = 0L
        while (true) {
            // The skip() method might skip beyond EOF. From FileInputStream's JavaDoc:
            // "This method may skip more bytes than what are remaining in the backing file."
            // (from https://docs.oracle.com/javase/8/docs/api/java/io/FileInputStream.html#skip-long-)
            // That's why we're using read instead.
            val countedBytes = stream.read(sharedByteArrayForCountingBytes)
            if (countedBytes == -1) break
            totalBytes += countedBytes
            onChunkRead(countedBytes, totalBytes)
        }
        totalBytes
    }
}
