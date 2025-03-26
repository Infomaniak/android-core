/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.core.webview.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import com.infomaniak.core.webview.ui.components.WebView
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val headers = intent.getStringExtra(EXTRA_HEADERS)
        val urlToQuit = intent.getStringExtra(EXTRA_URL_TO_QUIT)
        val url = intent.getStringExtra(EXTRA_URL)

        if (url == null) {
            finish()
            return
        }

        setContent {
            WebView(
                url = url,
                headersString = headers,
                onUrlToQuitReached = {
                    setResult(RESULT_OK)
                    finish()
                },
                urlToQuit = urlToQuit,
            )
        }
    }

    companion object {
        private const val EXTRA_URL = "EXTRA_URL"
        private const val EXTRA_HEADERS = "EXTRA_HEADERS"
        private const val EXTRA_URL_TO_QUIT = "EXTRA_URL_TO_QUIT"

        fun startActivity(
            context: Context,
            url: String,
            headers: Map<String, String>? = mapOf(),
            urlToQuit: String? = null,
            activityResultLauncher: ActivityResultLauncher<Intent>? = null,
        ) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_HEADERS, Json.encodeToString(headers))
                putExtra(EXTRA_URL_TO_QUIT, urlToQuit)
            }

            activityResultLauncher?.let {
                activityResultLauncher.launch(intent)
            } ?: run {
                context.startActivity(intent)
            }
        }
    }
}
