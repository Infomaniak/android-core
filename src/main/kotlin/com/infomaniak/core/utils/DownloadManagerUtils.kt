/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2025 Infomaniak Network SA
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
package com.infomaniak.core.utils

import android.app.DownloadManager
import android.app.DownloadManager.Request
import android.content.Context
import android.database.Cursor
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import androidx.core.net.toUri
import com.infomaniak.core.R
import com.infomaniak.core.extensions.appName
import com.infomaniak.core.extensions.appVersionName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DownloadManagerUtils {

    fun withoutProblematicCharacters(originalName: String): String {
        return originalName.replace(regexInvalidSystemChar, "_").replace('%', '_').let {
            // fix IllegalArgumentException only on Android 10 if multi dot
            if (SDK_INT == 29) it.replace(Regex("\\.{2,}"), ".") else it
        }
    }

    private val regexInvalidSystemChar = Regex("[\\\\/:*?\"<>|\\x7F]|[\\x00-\\x1f]")

    suspend fun requestFor(
        url: String,
        nameWithoutProblematicChars: String,
        mimeType: String?,
        userAgent: String,
        extraHeaders: Iterable<Pair<String, String>> = emptySet(),
    ): Request = Request(url.toUri()).also { request ->
        request.setAllowedNetworkTypes(Request.NETWORK_WIFI or Request.NETWORK_MOBILE)
        request.setTitle(nameWithoutProblematicChars)
        request.setDescription(appName)
        Dispatchers.IO {
            try {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameWithoutProblematicChars)
            } catch (e: IllegalStateException) {
                // The call above tries to create the directory if it doesn't exist,
                // and throws an `IllegalStateException` if its attempt at creating the directory fails…
                // but the reason it failed might because of a race condition,
                // like the DownloadManager app creating it in parallel, just after the `exists()` check,
                // and before the `mkdirs()` call tries to create the directory.
                // That's why we try once more if that case happens.
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameWithoutProblematicChars)
            }
        }
        request.setMimeType(mimeType)
        request.addHeaders(userAgent, extraHeaders)

        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    }

    private fun Request.addHeaders(userAgent: String, extraHeaders: Iterable<Pair<String, String>>) {
        addRequestHeader("Accept-Encoding", "gzip")
        addRequestHeader("App-Version", "Android $appVersionName")
        addRequestHeader("User-Agent", userAgent)
        extraHeaders.forEach { (key, value) -> addRequestHeader(key, value) }
    }

    fun scheduleDownload(
        context: Context,
        url: String,
        name: String,
        userBearerToken: String?,
        extraHeaders: Iterable<Pair<String, String>> = emptySet(),
        onError: (Int) -> Unit
    ) {
        val formattedName = name.replace(regexInvalidSystemChar, "_").replace("%", "_").let {
            // fix IllegalArgumentException only on Android 10 if multi dot
            if (SDK_INT == 29) it.replace(Regex("\\.{2,}"), ".") else it
        }

        Request(url.toUri()).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setTitle(formattedName)
            setDescription(appName)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, formattedName)
            extraHeaders.forEach { (key, value) -> addRequestHeader(key, value) }
            userBearerToken?.let { addRequestHeader("Authorization", "Bearer $it") }
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            handleDownloadManagerErrors(downloadManager.enqueue(this), downloadManager, onError)
        }
    }

    private fun handleDownloadManagerErrors(downloadReference: Long, downloadManager: DownloadManager, onError: (Int) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(1_000L)
            DownloadManager.Query().apply {
                setFilterById(downloadReference)
                downloadManager.query(this).use {
                    if (it.moveToFirst()) withContext(Dispatchers.Main) { checkStatus(it, onError) }
                }
            }
        }
    }

    private fun checkStatus(cursor: Cursor, onError: (Int) -> Unit) {
        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
        if (status == DownloadManager.STATUS_FAILED) {
            when (reason) {
                DownloadManager.ERROR_INSUFFICIENT_SPACE -> onError(R.string.errorDownloadInsufficientSpace)
                else -> onError(R.string.errorDownload)
            }
        }
    }
}
