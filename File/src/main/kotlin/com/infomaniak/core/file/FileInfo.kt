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
import android.provider.MediaStore
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
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.TimeSource
import kotlin.time.toJavaInstant

suspend fun fileNameFor(uri: Uri): String? = Dispatchers.IO {
    runCatching {
        uri.retrieveAndUse {
            getFileName(uri)
        }
    }.cancellable().onFailure { t -> SentryLog.e(TAG, "Failed to read the display name for uri: $uri", t) }.getOrNull()
}

suspend fun fileSizeFor(uri: Uri): Long = Dispatchers.IO {
    runCatching {
        uri.retrieveAndUse(sizeProjection) {
            getFileSize()
        } ?: -1L
    }.cancellable().onFailure { t ->
        SentryLog.e(TAG, "Failed to read the size for uri: $uri", t)
    }.getOrElse { -1L }
}

suspend fun getFileNameAndSize(uri: Uri): Pair<String, Long>? = Dispatchers.IO {
    runCatching {
        uri.retrieveAndUse {
            getFileName(uri) to getApproximativeFileSize(uri)
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

fun Cursor.getFileName(uri: Uri): String {
    return getStringFromColumns(fileNameColumns)
        ?: getNameFromDate()?.also { warnSentryFileName(uri, fileNameColumns, availableColumns = columnNames) }
        ?: uri.lastPathSegment?.also { warnSentryFileName(uri, nameAndDateColumns, availableColumns = columnNames) }
        ?: toString().also { alertSentryFileName(uri, nameAndDateColumns, columnNames) }
}

suspend fun Cursor.getApproximativeFileSize(uri: Uri): Long {
    return uri.statSize()
        ?: getFileSize()
        ?: uri.measureFileSize()
        ?: throw Exception("Cannot calculate size")
}

suspend fun Cursor.getPreciseFileSize(uri: Uri): Long? {
    return uri.statSize()
        ?: uri.measureFileSize()
        ?: getFileSize()
}

private fun Uri.statSize(): Long? {
    return runCatching {
        appCtx.contentResolver.openFileDescriptor(this, "r")
            ?.use { it.statSize }
            ?.takeUnless { it == -1L }
    }.getOrNull()
}

private fun Cursor.getFileSize(): Long? = getColumnIndexOrNull(OpenableColumns.SIZE)?.let(::getLongOrNull)

private fun Cursor.getStringFromColumns(orderedColumnNames: Array<String>): String? {
    return orderedColumnNames.firstNotNullOfOrNull(::getColumnIndexOrNull)?.let(::getStringOrNull)
}

@OptIn(ExperimentalTime::class)
private fun Cursor.getNameFromDate(): String? = getDate()?.toJavaInstant()?.let { simpleDateFormater.format(it) }

@OptIn(ExperimentalTime::class)
private fun Cursor.getDate(): Instant? {
    return getColumnIndexOrNull(MediaStore.MediaColumns.DATE_ADDED)
        ?.let(::getLongOrNull)
        ?.let(Instant::fromEpochSeconds)
}

private fun Cursor.getColumnIndexOrNull(columnName: String) = getColumnIndex(columnName).takeUnless { it == -1 }

private fun warnSentryFileName(uri: Uri, columns: Array<String>, availableColumns: Array<String>) {
    SentryLog.w(
        tag = TAG,
        msg = "GetFileName not went well",
        throwable = FileNameException(uri, columns, availableColumns)
    )
}

private fun alertSentryFileName(uri: Uri, allColumns: Array<String>, columnNames: Array<String>) {
    Sentry.captureException(FileNameException(uri, allColumns, columnNames, IllegalArgumentException("No path segment")))
}

private class FileNameException(
    uri: Uri,
    searchedColumns: Array<String>,
    availableColumns: Array<String>,
    cause: Throwable? = null
) : Throwable(
    message = "No file name retrievable for uri $uri " +
            "Columns searched ${searchedColumns.toList()} columns but available columns : ${availableColumns.toList()}",
    cause = cause
)

/**
 * Queries the cursor for an Uri with [projection], and if found apply [block]
 */
private suspend fun <R> Uri.retrieveAndUse(projection: Array<String>? = null, block: suspend Cursor.() -> R?): R? {
    return appCtx.contentResolver.query(
        /* uri = */ this,
        // Not supplying a projection might lead to `NullPointerException` with message "Attempt to get length of null array"
        // being thrown on some devices, despite what is written in the Javadoc, so we provide one.
        /* projection = */ projection ?: emptyArray(),
        /* selection = */ null,
        /* selectionArgs = */ null,
        /* sortOrder = */ null
    )?.use {
        if (it.moveToFirst()) {
            block(it)
        } else {
            Sentry.captureMessage("$appCtx has empty cursor") { scope ->
                scope.setExtra("available columns", it.columnNames.joinToString())
            }
            null
        }
    }
}

private val counter = InputStreamCounter()
private const val TAG = "FileInfo"
private val sizeProjection by lazy { arrayOf(OpenableColumns.SIZE) }
private val fileNameColumns by lazy { arrayOf(OpenableColumns.DISPLAY_NAME, "name") }
private val nameAndDateColumns by lazy { fileNameColumns + MediaStore.MediaColumns.DATE_ADDED }
private val simpleDateFormater by lazy { DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss") }
