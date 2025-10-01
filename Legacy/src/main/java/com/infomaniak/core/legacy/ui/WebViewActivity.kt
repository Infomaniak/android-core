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
package com.infomaniak.core.legacy.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.infomaniak.core.legacy.databinding.ActivityWebviewBinding
import kotlinx.serialization.json.Json

class WebViewActivity : AppCompatActivity() {

    private val binding by lazy { ActivityWebviewBinding.inflate(layoutInflater) }
    private val navArgs: WebViewActivityArgs by navArgs()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.webview.apply {
            webViewClient = CustomWebViewClient(
                urlToQuit = navArgs.urlToQuit,
                onUrlToQuitReached = {
                    setResult(RESULT_OK)
                    finish()
                },
            )
            settings.javaScriptEnabled = true
            val headers = navArgs.headers?.let { Json.decodeFromString<Map<String, String>>(it) } ?: mapOf()
            loadUrl(navArgs.url, headers)
        }
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

    companion object {

        fun startActivity(
            context: Context,
            url: String,
            headers: Map<String, String>? = mapOf(),
            urlToQuit: String? = null,
            activityResultLauncher: ActivityResultLauncher<Intent>? = null,
        ) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                val webViewActivityArgs = WebViewActivityArgs(url, Json.encodeToString(headers), urlToQuit)
                putExtras(webViewActivityArgs.toBundle())
            }
            activityResultLauncher?.let {
                activityResultLauncher.launch(intent)
            } ?: run {
                context.startActivity(intent)
            }
        }
    }
}
