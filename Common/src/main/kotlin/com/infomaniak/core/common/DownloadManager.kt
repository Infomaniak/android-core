/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2026 Infomaniak Network SA
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
package com.infomaniak.core.common

import android.app.DownloadManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import splitties.mainhandler.mainHandler

@JvmInline
value class UniqueDownloadId(val value: Long)

sealed interface DownloadStatus {

    data class Failed(val reason: Reason) : DownloadStatus {

        sealed interface Reason {
            data object CannotResume : Reason
            data object UnknownError : Reason
        }

        sealed interface LocalIssue : Reason {
            data object InsufficientSpace : LocalIssue
            data object StorageDeviceNotFound : LocalIssue
            data object FileError : LocalIssue
            data object FileAlreadyExists : LocalIssue
        }

        sealed interface RemoteIssue : Reason {
            data class HttpError(val statusCode: Int) : RemoteIssue
            data object HttpDataError : RemoteIssue
            data object TooManyRedirects : RemoteIssue
            data object UnhandledHttpCode : RemoteIssue

        }
    }

    data class Paused(val reason: Reason?) : DownloadStatus {
        enum class Reason {
            WaitingToRetry,
            WaitingForNetwork,
            QueuedForWifi;
        }
    }

    data object Pending : DownloadStatus

    data class InProgress(
        val downloadedBytes: Long,
        val totalSizeInBytes: Long,
    ) : DownloadStatus

    data object Complete : DownloadStatus
}

suspend fun DownloadManager.startDownloadingFile(request: DownloadManager.Request): UniqueDownloadId? {
    val uniqueDownloadId = Dispatchers.IO {
        try {
            enqueue(request)
        } catch (_: NullPointerException) {
            // enqueue is supposed to return -1 in case the operation fails,
            // but on Xiaomi and Redmi devices, it throws NPEs instead…
            -1L
        }
    }
    return if (uniqueDownloadId == -1L) null else UniqueDownloadId(uniqueDownloadId)
}

fun DownloadManager.downloadStatusFlow(
    id: UniqueDownloadId
): Flow<DownloadStatus?> = callbackFlow {
    val query = DownloadManager.Query().also { it.setFilterById(id.value) }
    val contentObserver = object : ContentObserver(mainHandler) {

        override fun onChange(selfChange: Boolean) {
            launch(Dispatchers.IO) {
                query(query).use { cursor: Cursor? ->
                    val currentStatus = cursor.extractDownloadStatus()
                    trySend(currentStatus)
                }
            }
        }
    }
    Dispatchers.IO {
        val cursor: Cursor? = query(query)
        if (cursor == null) {
            trySend(null)
            close()
            awaitCancellation()
        }
        cursor.use {
            it.registerContentObserver(contentObserver)
            val currentStatus = it.extractDownloadStatus()
            trySend(currentStatus)
            try {
                awaitCancellation()
            } finally {
                it.unregisterContentObserver(contentObserver)
            }
        }
    }
}.flowOn(Dispatchers.Default).conflate()

suspend fun DownloadManager.doesFileExist(id: UniqueDownloadId): Boolean {
    val uri = Dispatchers.IO { getUriForDownloadedFile(id.value) } ?: return false
    return Dispatchers.IO { uri.doesFileExist() }
}

suspend fun DownloadManager.cancelAndRemove(id: UniqueDownloadId): Boolean {
    return Dispatchers.IO { remove(id.value) } == 1
}

suspend fun DownloadManager.uriFor(id: UniqueDownloadId): Uri? {
    return Dispatchers.IO { getUriForDownloadedFile(id.value) }
}

private fun Cursor?.extractDownloadStatus(): DownloadStatus? {
    if (this == null) return null // Android 14 Redmi Note 13 can have query return a null value.
    if (moveToFirst().not()) return null
    return buildStatus(
        statusInt = getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)),
        reason = getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)),
        downloadedBytes = getLong(getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
        totalSizeInBytes = getLong(getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
    )
}

private fun buildStatus(
    statusInt: Int,
    reason: Int,
    downloadedBytes: Long,
    totalSizeInBytes: Long
): DownloadStatus = when (statusInt) {
    DownloadManager.STATUS_RUNNING -> DownloadStatus.InProgress(
        downloadedBytes = downloadedBytes,
        totalSizeInBytes = totalSizeInBytes
    )
    DownloadManager.STATUS_PENDING -> DownloadStatus.Pending
    DownloadManager.STATUS_PAUSED -> DownloadStatus.Paused(reason = buildPausedReason(reason))
    DownloadManager.STATUS_FAILED -> DownloadStatus.Failed(reason = buildFailureReason(statusInt = statusInt, reason = reason))
    DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.Complete
    else -> error("Unexpected download status: $statusInt reason: $reason")
}

private fun buildFailureReason(statusInt: Int, reason: Int): DownloadStatus.Failed.Reason = when (reason) {
    DownloadManager.ERROR_INSUFFICIENT_SPACE -> DownloadStatus.Failed.LocalIssue.InsufficientSpace
    DownloadManager.ERROR_DEVICE_NOT_FOUND -> DownloadStatus.Failed.LocalIssue.StorageDeviceNotFound
    DownloadManager.ERROR_FILE_ERROR -> DownloadStatus.Failed.LocalIssue.FileError
    DownloadManager.ERROR_CANNOT_RESUME -> DownloadStatus.Failed.Reason.CannotResume
    DownloadManager.ERROR_HTTP_DATA_ERROR -> DownloadStatus.Failed.RemoteIssue.HttpDataError
    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> DownloadStatus.Failed.RemoteIssue.TooManyRedirects
    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> DownloadStatus.Failed.LocalIssue.FileAlreadyExists
    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> DownloadStatus.Failed.RemoteIssue.UnhandledHttpCode
    DownloadManager.ERROR_UNKNOWN -> DownloadStatus.Failed.Reason.UnknownError
    else -> DownloadStatus.Failed.RemoteIssue.HttpError(statusCode = statusInt)
}

private fun buildPausedReason(reason: Int): DownloadStatus.Paused.Reason? = when (reason) {
    DownloadManager.PAUSED_QUEUED_FOR_WIFI -> DownloadStatus.Paused.Reason.QueuedForWifi
    DownloadManager.PAUSED_WAITING_TO_RETRY -> DownloadStatus.Paused.Reason.WaitingToRetry
    DownloadManager.PAUSED_WAITING_FOR_NETWORK -> DownloadStatus.Paused.Reason.WaitingForNetwork
    else -> null
}
