/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.core.ui.compose.contactcard

import android.content.Context
import android.util.Base64
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.common.cancellable
import com.infomaniak.core.network.networking.DefaultHttpClientProvider.okHttpClient
import com.infomaniak.core.network.utils.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

private const val MAX_AVATAR_SIZE = 5 * 1024 * 1024 // 5 MB
private val ILLEGAL_FILE_NAME_CHARACTERS = Regex("[\\\\/:*?\"<>|]+")

suspend fun Card.createShareFile(context: Context, avatarData: Pair<String?, String?>): File = withContext(Dispatchers.IO) {
    val fileName = "attachments_cache${firstName}_${lastName}.vcf"
    val safeFileName = fileName.replace(ILLEGAL_FILE_NAME_CHARACTERS, "")

    val directory = File(context.cacheDir, "attachments_cache").apply { mkdirs() }

    File(directory, safeFileName).apply {
        val vCardContent = makeVCardString(avatarBase64 = avatarData.first, avatarMimeType = avatarData.second)
        writeText(vCardContent)
    }
}

suspend fun Card.getAvatarDataOrNull(): Pair<String?, String?> {
    val url = avatarUrl ?: return null to null

    return runCatching {
        val request = Request.Builder().url(url).build()

        okHttpClient.newCall(request).await().use { response ->
            val body = response.body
            if (!response.isSuccessful) return@runCatching null to null

            val contentLength = body.contentLength()
            if (contentLength > MAX_AVATAR_SIZE || contentLength == (-1).toLong()) return@runCatching null to null

            val mimeType = body.contentType()?.subtype?.uppercase()
            val bytes = body.bytes()

            if (bytes.size > MAX_AVATAR_SIZE) return@runCatching null to null

            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            base64 to mimeType
        }
    }
        .cancellable()
        .getOrDefault(null to null)
}
