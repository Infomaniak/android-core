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
package com.infomaniak.lib.core.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.database.getStringOrNull
import io.sentry.Sentry
import java.net.URLDecoder

fun Cursor.getFileName(uri: Uri): String {
    val filename = runCatching {
        getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(this::getStringOrNull)
    }.getOrElse { exception ->
        Sentry.captureException(exception)
        null
    }

    return filename ?: URLDecoder.decode(uri.toString(), "UTF-8").substringAfterLast("/")
}

fun Cursor.getFileSize() = getLong(getColumnIndexOrThrow(OpenableColumns.SIZE))

fun Context.getFileNameAndSize(uri: Uri): Pair<String, Long>? {
    return runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val fileName = cursor.getFileName(uri)
                val fileSize = runCatching {
                    cursor.getFileSize()
                }.getOrElse {
                    uri.calculateFileSize(contentResolver)
                } ?: throw Exception("Cannot calculate size")

                fileName to fileSize
            } else {
                Sentry.withScope { scope ->
                    scope.setExtra("available columns", cursor.columnNames.joinToString { it })
                    Sentry.captureMessage("$this has empty cursor")
                }
                null
            }
        }
    }.getOrElse { exception ->
        uri.path?.substringBeforeLast("/")?.let { providerName ->
            Sentry.withScope { scope ->
                scope.setExtra("uri", providerName)
                Sentry.captureException(exception)
            }
        }
        null
    }
}

/**
 * Calculates the size of a file from a Uri.
 * @return The file size in bytes, or null if there is an error during the calculation.
 */
fun Uri.calculateFileSize(contentResolver: ContentResolver): Long? {
    return runCatching {
        contentResolver.openInputStream(this)?.use { inputStream ->
            var currentSize: Int
            val byteArray = ByteArray(8192) // 8 Ko
            var fileSize = 0L
            while (inputStream.read(byteArray).also { currentSize = it } != -1) {
                fileSize += currentSize
            }
            fileSize
        }
    }.getOrNull()
}
