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

package com.infomaniak.core.webview.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.serialization.json.Json

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(
    url: String,
    headersString: String?,
    onUrlToQuitReached: () -> Unit,
    urlToQuit: String?,
    domStorageEnabled: Boolean = false,
) {
    AndroidView(
        modifier = Modifier.safeDrawingPadding(),
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = CustomWebViewClient(
                    urlToQuit = urlToQuit,
                    onUrlToQuitReached = onUrlToQuitReached,
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = domStorageEnabled

                val headers = headersString?.let { Json.decodeFromString<Map<String, String>>(it) } ?: mapOf()
                loadUrl(url, headers)
            }
        })
}

private class CustomWebViewClient(
    private val urlToQuit: String?,
    private val onUrlToQuitReached: () -> Unit,
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
        return if (urlToQuit != null && request?.url?.toString()?.contains(urlToQuit) == true) {
            onUrlToQuitReached()
            true
        } else {
            super.shouldOverrideUrlLoading(view, request)
        }
    }
}
