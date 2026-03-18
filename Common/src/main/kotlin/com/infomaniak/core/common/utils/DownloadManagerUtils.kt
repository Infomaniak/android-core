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
package com.infomaniak.core.common.utils

import android.app.DownloadManager.Request
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import androidx.core.net.toUri
import com.infomaniak.core.common.DownloadStatus
import com.infomaniak.core.common.DownloadStatus.Companion.isFinished
import com.infomaniak.core.common.DownloadStatus.Failed
import com.infomaniak.core.common.DownloadStatus.Failed.LocalIssue
import com.infomaniak.core.common.R
import com.infomaniak.core.common.downloadStatusFlow
import com.infomaniak.core.common.extensions.appName
import com.infomaniak.core.common.extensions.appVersionName
import com.infomaniak.core.common.startDownloadingFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.systemservices.downloadManager

object DownloadManagerUtils {
    val regexInvalidSystemChar = Regex("[\\\\/:*?\"<>|\\x7F]|[\\x00-\\x1f]|%")

    fun withoutProblematicCharacters(name: String): String = name.sanitizeNameForDownload()

    private fun String.sanitizeNameForDownload() = replace(regexInvalidSystemChar, "_").fixMultiDots()

    private fun String.fixMultiDots() = apply { if (SDK_INT == Build.VERSION_CODES.Q) replace(Regex("\\.{2,}"), ".") }

    suspend fun requestFor(
        url: String,
        nameWithoutProblematicChars: String,
        mimeType: String? = null,
        userAgent: String,
        extraHeaders: Iterable<Pair<String, String>> = emptySet(),
    ): Request = Request(url.toUri())
        .setAllowedNetworkTypes(Request.NETWORK_WIFI or Request.NETWORK_MOBILE)
        .setTitle(nameWithoutProblematicChars)
        .setDescription(appName)
        .setPublicDownloadDirectoryDestination(nameWithoutProblematicChars)
        .setMimeType(mimeType)
        .addHeaders(userAgent, extraHeaders)
        .setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    private suspend fun Request.setPublicDownloadDirectoryDestination(nameWithoutProblematicChars: String) = Dispatchers.IO {
        try {
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameWithoutProblematicChars)
        } catch (_: IllegalStateException) {
            // The call above tries to create the directory if it doesn't exist,
            // and throws an `IllegalStateException` if its attempt at creating the directory fails…
            // but the reason it failed might because of a race condition,
            // like the DownloadManager app creating it in parallel, just after the `exists()` check,
            // and before the `mkdirs()` call tries to create the directory.
            // That's why we try once more if that case happens.
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameWithoutProblematicChars)
        }
    }

    private fun Request.addHeaders(
        userAgent: String,
        extraHeaders: Iterable<Pair<String, String>>
    ): Request = addRequestHeader("Accept-Encoding", "gzip")
        .addRequestHeader("App-Version", "Android $appVersionName")
        .addRequestHeader("User-Agent", userAgent)
        .addRequestHeaders(extraHeaders)

    private fun Request.addRequestHeaders(headers: Iterable<Pair<String, String>>): Request = apply {
        headers.forEach { addRequestHeader(pair = it) }
    }

    private fun Request.addRequestHeader(pair: Pair<String, String>): Request = with(pair) {
        addRequestHeader(first, second)
    }

    fun launchDownload(
        url: String,
        name: String,
        userAgent: String,
        userBearerToken: String?,
        onError: (Int) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            val request = requestFor(
                url = url,
                nameWithoutProblematicChars = name.sanitizeNameForDownload(),
                userAgent = userAgent,
                extraHeaders = listOfNotNull(userBearerToken?.asAuthorizationHeader())
            )
            with(downloadManager) {
                startDownloadingFile(request)?.let(::downloadStatusFlow)?.observeEnd(onError)
            }
        }
    }

    private fun String.asAuthorizationHeader(): Pair<String, String> = "Authorization" to "Bearer $this"

    private suspend fun Flow<DownloadStatus?>.observeEnd(onError: (Int) -> Unit) {
        filter { it.isFinished() }.first().checkFailure(onError)
    }

    private suspend fun DownloadStatus?.checkFailure(onError: (Int) -> Unit) {
        (this as? Failed)?.run {
            withContext(Dispatchers.Main) {
                when (reason) {
                    LocalIssue.InsufficientSpace -> onError(R.string.errorDownloadInsufficientSpace)
                    else -> onError(R.string.errorDownload)
                }
            }
        }
    }
}
