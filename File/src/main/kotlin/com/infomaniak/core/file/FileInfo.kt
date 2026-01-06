/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.infomaniak.core.sentry.SentryLog
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import splitties.init.appCtx
import java.net.URLDecoder

suspend fun fileNameFor(uri: Uri): String? = Dispatchers.IO {
    runCatching {
        appCtx.contentResolver.query(
            /* uri = */ uri,
            // Not supplying a projection might lead to `NullPointerException` with message "Attempt to get length of null array"
            // being thrown on some devices, despite what is written in the Javadoc, so we provide one.
            /* projection = */ displayNameProjection,
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        )?.use { cursor: Cursor ->
            if (cursor.moveToFirst()) {
                cursor.getFileName()
            } else null
        }
    }.onFailure { t -> SentryLog.e(TAG, "Failed to read the display name for uri: $uri", t) }.getOrNull()
}

private fun Cursor.getFileName(): String? {
    return when (val nameColumnIndex = getColumnIndex(OpenableColumns.DISPLAY_NAME)) {
        -1 -> null
        else -> getStringOrNull(nameColumnIndex)
    }
}

suspend fun fileSizeFor(uri: Uri): Long = Dispatchers.IO {
    runCatching {
        appCtx.contentResolver.query(
            /* uri = */ uri,
            // Not supplying a projection might lead to `NullPointerException` with message "Attempt to get length of null array"
            // being thrown on some devices, despite what is written in the Javadoc, so we provide one.
            /* projection = */ sizeProjection,
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        )?.use { cursor: Cursor ->
            if (cursor.moveToFirst()) {
                cursor.getFileSize()
            } else -1L
        } ?: -1L
    }.onFailure { t ->
        SentryLog.e(TAG, "Failed to read the size for uri: $uri", t)
    }.getOrElse { -1L }
}

private fun Cursor.getFileSize(): Long {
    return when (val sizeColumnIndex = getColumnIndex(OpenableColumns.SIZE)) {
        -1 -> -1L
        else -> getLongOrNull(sizeColumnIndex)
    } ?: -1
}

suspend fun getFileNameAndSize(uri: Uri): Pair<String, Long>? = Dispatchers.IO {
    runCatching {
        val contentResolver = appCtx.contentResolver
        contentResolver.query(uri, displayNameProjection + sizeProjection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val fileName = cursor.getFileName() ?: URLDecoder.decode(uri.toString(), "UTF-8").substringAfterLast("/")
                val fileSize = runCatching {
                    cursor.getFileSize()
                }.getOrElse {
                    uri.calculateFileSize(contentResolver)
                } ?: throw Exception("Cannot calculate size")

                fileName to fileSize
            } else {
                Sentry.captureMessage("$appCtx has empty cursor") { scope ->
                    scope.setExtra("available columns", cursor.columnNames.joinToString())
                }
                null
            }
        }
    }.getOrElse { exception ->
        uri.path?.substringBeforeLast("/")?.let { providerName ->
            Sentry.captureException(exception) { scope ->
                scope.setExtra("uri", providerName)
            }
        }
        null
    }
}

/**
 * Calculates the size of a file from a Uri.
 * @return The file size in bytes, or null if there is an error during the calculation.
 */
private fun Uri.calculateFileSize(contentResolver: ContentResolver): Long? {
    return runCatching {
        contentResolver.openInputStream(this)?.use { inputStream ->
            var currentSize: Int
            val byteArray = ByteArray(8_192) // 8 Ko
            var fileSize = 0L
            while (inputStream.read(byteArray).also { currentSize = it } != -1) {
                fileSize += currentSize
            }
            fileSize
        }
    }.getOrNull()
}


private const val TAG = "FileInfo"

private val displayNameProjection = arrayOf(OpenableColumns.DISPLAY_NAME)
private val sizeProjection = arrayOf(OpenableColumns.SIZE)
