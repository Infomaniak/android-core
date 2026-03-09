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

import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.infomaniak.core.common.cancellable
import com.infomaniak.core.sentry.SentryLog
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.invoke
import kotlinx.coroutines.yield
import splitties.init.appCtx
import java.net.URLDecoder
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

suspend fun fileNameFor(uri: Uri): String? = Dispatchers.IO {
    runCatching {
        uri.retrieveAndUse(displayNameProjection) {
            if (moveToFirst()) {
                getFileName()
            } else null
        }
    }.cancellable().onFailure { t -> SentryLog.e(TAG, "Failed to read the display name for uri: $uri", t) }.getOrNull()
}

private fun Cursor.getFileName(): String? {
    return when (val nameColumnIndex = getColumnIndex(OpenableColumns.DISPLAY_NAME)) {
        -1 -> null
        else -> getStringOrNull(nameColumnIndex)
    }
}

suspend fun fileSizeFor(uri: Uri): Long = Dispatchers.IO {
    runCatching {
        uri.retrieveAndUse(sizeProjection) {
            if (moveToFirst()) {
                getFileSize()
            } else -1L
        } ?: -1L
    }.cancellable().onFailure { t ->
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
        uri.retrieveAndUse(displayNameProjection + sizeProjection) {
            if (moveToFirst()) {
                val fileName = getFileName() ?: URLDecoder.decode(uri.toString(), "UTF-8").substringAfterLast("/")
                val fileSize = runCatching {
                    getFileSize()
                }.getOrElse {
                    uri.measureFileSize()
                } ?: throw Exception("Cannot calculate size")

                fileName to fileSize
            } else {
                Sentry.captureMessage("$appCtx has empty cursor") { scope ->
                    scope.setExtra("available columns", columnNames.joinToString())
                }
                null
            }
        }
    }.cancellable().getOrElse { exception ->
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
private suspend fun Uri.measureFileSize(): Long? = runCatching {
    val yieldInterval = 1.seconds
    var lastYield = TimeSource.Monotonic.markNow()
    counter.fileSizeFor(
        uri = this@measureFileSize,
        onChunkRead = { _, _ ->
            if (lastYield.elapsedNow() >= yieldInterval) {
                lastYield = TimeSource.Monotonic.markNow()
                yield()
            } else {
                currentCoroutineContext().ensureActive()
            }
        }
    )
}.cancellable().getOrNull()

/**
 * Queries the cursor for an Uri with [projection], and if found apply [block]
 */
private suspend fun <R> Uri.retrieveAndUse(projection: Array<String>, block: suspend Cursor.() -> R?): R? {
    return appCtx.contentResolver.query(
        /* uri = */ this,
        // Not supplying a projection might lead to `NullPointerException` with message "Attempt to get length of null array"
        // being thrown on some devices, despite what is written in the Javadoc, so we provide one.
        /* projection = */ projection,
        /* selection = */ null,
        /* selectionArgs = */ null,
        /* sortOrder = */ null
    )?.use { block(it) }
}

private val counter = InputStreamCounter()
private const val TAG = "FileInfo"

private val displayNameProjection = arrayOf(OpenableColumns.DISPLAY_NAME)
private val sizeProjection = arrayOf(OpenableColumns.SIZE)
