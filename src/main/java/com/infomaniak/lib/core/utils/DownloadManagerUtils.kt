/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.networking.HttpUtils
import kotlinx.coroutines.*

object DownloadManagerUtils {

    val regexInvalidSystemChar = Regex("[\\\\/:*?\"<>|\\x7F]|[\\x00-\\x1f]")

    fun scheduleDownload(context: Context, url: String, name: String) {
        val formattedName = name.replace(regexInvalidSystemChar, "_").replace("%", "_").let {
            // fix IllegalArgumentException only on Android 10 if multi dot
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) it.replace(Regex("\\.{2,}"), ".") else it
        }

        DownloadManager.Request(Uri.parse(url)).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setTitle(formattedName)
            setDescription(context.getAppName())
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, formattedName)
            HttpUtils.getHeaders(contentType = null).toMap().forEach { addRequestHeader(it.key, it.value) }
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            context.handleDownloadManagerErrors(downloadManager.enqueue(this), downloadManager)
        }
    }

    private fun Context.handleDownloadManagerErrors(downloadReference: Long, downloadManager: DownloadManager) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            DownloadManager.Query().apply {
                setFilterById(downloadReference)
                downloadManager.query(this).use {
                    if (it.moveToFirst()) withContext(Dispatchers.Main) { checkStatus(it) }
                }
            }
        }
    }

    private fun Context.checkStatus(cursor: Cursor) {
        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
        if (status == DownloadManager.STATUS_FAILED) {
            when (reason) {
                DownloadManager.ERROR_INSUFFICIENT_SPACE -> showToast(R.string.errorDownloadInsufficientSpace)
                else -> showToast(R.string.errorDownload)
            }
        }
    }
}
