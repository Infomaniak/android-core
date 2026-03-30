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
import android.os.Build.VERSION.SDK_INT
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.infomaniak.core.common.cancellable
import com.infomaniak.core.common.utils.FORMAT_NEW_FILE
import com.infomaniak.core.sentry.SentryLog
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.invoke
import kotlinx.coroutines.yield
import splitties.init.appCtx
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

val DATE_TAKEN: String = if (SDK_INT >= 29) MediaStore.MediaColumns.DATE_TAKEN else "datetaken"

private val sizeProjection by lazy { arrayOf(OpenableColumns.SIZE) }
private val displayNameProjection by lazy { arrayOf(OpenableColumns.DISPLAY_NAME) }
private val displayNameAndSizeProjection by lazy { displayNameProjection + sizeProjection }
private val displayNameAndDateAddedProjection by lazy { displayNameProjection + MediaStore.MediaColumns.DATE_ADDED }
private val simpleDateFormatter by lazy { SimpleDateFormat(FORMAT_NEW_FILE, Locale.getDefault()) }
private val dateTimeRegex by lazy { ".*${DATE_AND_TIME}(?:_\\d+)?\\..*".toRegex() }
private val counter = InputStreamCounter()
private const val TAG = "FileInfo"
/* language=RegExp */
private const val DATE_AND_TIME = "(\\d{8}_\\d{6})"

suspend fun fileNameFor(uri: Uri): String? = uri.retrieveAndUse(displayNameProjection) { getFileName(uri) }

suspend fun fileSizeFor(uri: Uri): Long = uri.retrieveAndUse(sizeProjection) { getFileSize() } ?: -1L

suspend fun getFileNameAndSize(uri: Uri): Pair<String, Long>? = uri.retrieveAndUse(displayNameAndSizeProjection) {
    getFileName(uri) to getEstimatedFileSize(uri)
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
    return getDisplayName()
        ?: getNameFromDate()?.also { warnSentryFileName(uri, displayNameProjection, availableColumns = columnNames) }
        ?: uri.lastPathSegment?.also {
            warnSentryFileName(uri, displayNameAndDateAddedProjection, availableColumns = columnNames)
        }
        ?: uri.toString().also { alertSentryFileName(uri, displayNameAndDateAddedProjection, columnNames) }
}

suspend fun Cursor.getEstimatedFileSize(uri: Uri): Long {
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

private fun Cursor.getDisplayName(): String? = getColumnIndexOrNull(OpenableColumns.DISPLAY_NAME)?.let(::getStringOrNull)

private fun Cursor.getFileSize(): Long? = getLongOrNull(OpenableColumns.SIZE)

private fun Cursor.getNameFromDate(): String? = getDateAdded()?.toInstant()?.let(simpleDateFormatter::format)

fun Cursor.getFileDatesWithFallback(lastModifiedDateFallback: Date? = null): Pair<Date?, Date> {
    val createdAt = getFileCreatedDate()
    val modifiedAt = getFileUpdatedDate() ?: lastModifiedDateFallback ?: createdAt ?: Date()
    return createdAt to modifiedAt
}

private fun Cursor.getFileCreatedDate(): Date? = getDateTaken() ?: attemptExtractFromName() ?: getDateAdded()

private fun Cursor.getFileUpdatedDate(): Date? = getLastModified() ?: getDateModified()

private fun Cursor.getDateTaken(): Date? = getDateFromMillisecond(DATE_TAKEN)

private fun Cursor.getDateAdded(): Date? = getDateFromSecond(MediaStore.MediaColumns.DATE_ADDED)

private fun Cursor.attemptExtractFromName(): Date? {
    return getDisplayName()
        ?.let { dateTimeRegex.find(it)?.groups[1]?.value }
        ?.let { runCatching { simpleDateFormatter.parse(it) }.getOrNull() }
}

fun Cursor.getLongOrNull(columnName: String): Long? = getColumnIndexOrNull(columnName)?.let(::getLongOrNull)

private fun Cursor.getLastModified(): Date? = getDateFromMillisecond(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

private fun Cursor.getDateModified(): Date? = getDateFromSecond(MediaStore.MediaColumns.DATE_MODIFIED)

private fun Cursor.getDateFromSecond(columnName: String): Date? {
    return getLongOrNull(columnName)?.run { Date(seconds.inWholeMilliseconds) }.isValid()
}

private fun Cursor.getDateFromMillisecond(columnName: String): Date? {
    return getLongOrNull(columnName)?.let(::Date).isValid()
}

private fun Date?.isValid() = this?.takeIf { it.time > 0 }

private fun Cursor.getColumnIndexOrNull(columnName: String) = getColumnIndex(columnName).takeUnless { it == -1 }

private fun warnSentryFileName(uri: Uri, columns: Array<String>, availableColumns: Array<String>) {
    SentryLog.w(
        tag = TAG,
        msg = "GetFileName didn't go well",
        throwable = FileNameException(uri, columns, availableColumns)
    )
}

private fun alertSentryFileName(uri: Uri, allColumns: Array<String>, columnNames: Array<String>) {
    val exception = FileNameException(uri, allColumns, columnNames, IllegalArgumentException("No path segment"))
    SentryLog.e(TAG, "Error retrieving fileName in FileProvider", exception)
}

/**
 * Queries the cursor for an Uri with [projection], and if found apply [block].
 * Can throw a NullPointerException with message "Attempt to get length of null array" on some devices,
 * despite what is written in the Javadoc, so we provide one, so there is a runCatching to catch it
 */
suspend fun <R> Uri.retrieveAndUse(projection: Array<String>? = null, block: suspend Cursor.() -> R?): R? = runCatching {
    Dispatchers.IO {
        appCtx.contentResolver.query(this@retrieveAndUse, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                block(cursor)
            } else {
                cursor.logProjectionFailure(uri = this@retrieveAndUse)
                null
            }
        }
    }
}.cancellable().getOrNull()

private fun Cursor.logProjectionFailure(uri: Uri) {
    val columns = columnNames.joinToString()
    SentryLog.i(TAG, "$appCtx has empty cursor available columns $columns")
    Sentry.captureMessage("$appCtx has empty cursor") { scope ->
        scope.setExtra("uri", uri.toString())
        scope.setExtra("available columns", columns)
    }
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
