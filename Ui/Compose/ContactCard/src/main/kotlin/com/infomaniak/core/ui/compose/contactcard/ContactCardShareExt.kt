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
import android.content.Intent
import android.util.Base64
import androidx.core.content.FileProvider
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.network.networking.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

private const val CONTACT_CARD_DIRECTORY = "attachments_cache"
private const val CONTACT_CARD_FILE_PREFIX = "contact_card_"
private const val CONTACT_CARD_FILE_SUFFIX = ".vcf"

suspend fun Card.createShareFile(context: Context): File = withContext(Dispatchers.IO) {
    val avatarBase64 = avatarUrl?.let { url ->
        runCatching {
            val request = Request.Builder().url(url).build()
            HttpClient.okHttpClient.newCall(request).execute().use { response ->
                response.takeIf { it.isSuccessful }
                    ?.body?.bytes()
                    ?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
            }
        }.getOrNull()
    }

    val file = File(
        context.cacheDir,
        "$CONTACT_CARD_DIRECTORY/$CONTACT_CARD_FILE_PREFIX$email$CONTACT_CARD_FILE_SUFFIX",
    )
    file.parentFile?.mkdirs()
    file.writeText(makeVCardString(avatarBase64 = avatarBase64))
    file
}

suspend fun Context.shareContactCard(card: Card) {
    val file = card.createShareFile(this)
    val uri = FileProvider.getUriForFile(this, getString(R.string.ATTACHMENTS_AUTHORITY), file)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/vcard"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(Intent.createChooser(intent, getString(R.string.contactCardShareChooserTitle)))
}
