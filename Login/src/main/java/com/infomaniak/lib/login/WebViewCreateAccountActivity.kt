/*
 * Infomaniak kMail - Android
 * Copyright (C) 2023 Infomaniak Network SA
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
package com.infomaniak.lib.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.infomaniak.lib.login.InfomaniakLogin.Companion.CANCEL_HOST_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.CREATE_ACCOUNT_URL_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.HEADERS_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.IGNORE_FIRST_CANCEL_URL_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.REMOVE_COOKIES_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.SUCCESS_HOST_TAG
import com.infomaniak.lib.login.databinding.ActivityWebViewLoginBinding
import com.infomaniak.lib.login.ext.handleEdgeToEdge
import kotlinx.serialization.json.Json
import java.util.MissingFormatArgumentException

class WebViewCreateAccountActivity : AppCompatActivity() {

    private val binding by lazy { ActivityWebViewLoginBinding.inflate(layoutInflater) }

    private val createAccountUrl: String by lazy {
        intent.getStringExtra(CREATE_ACCOUNT_URL_TAG) ?: throw MissingFormatArgumentException(CREATE_ACCOUNT_URL_TAG)
    }
    private val successUrl: String by lazy {
        intent.getStringExtra(SUCCESS_HOST_TAG) ?: throw MissingFormatArgumentException(SUCCESS_HOST_TAG)
    }
    private val cancelUrl: String by lazy {
        intent.getStringExtra(CANCEL_HOST_TAG) ?: throw MissingFormatArgumentException(CANCEL_HOST_TAG)
    }
    private val removeCookies: Boolean by lazy {
        intent.getBooleanExtra(REMOVE_COOKIES_TAG, true)
    }
    private val headers: Map<String, String> by lazy {
        intent.getStringExtra(HEADERS_TAG)?.let {
            Json.decodeFromString<Map<String, String>>(it)
        } ?: mapOf()
    }
    private val ignoreFirstCancelUrl: Boolean by lazy {
        intent.getBooleanExtra(IGNORE_FIRST_CANCEL_URL_TAG, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        if (removeCookies) WebViewUtils.flushAllCookies()

        binding.handleEdgeToEdge()

        binding.toolbar.title = getString(R.string.create_account)
        binding.webview.apply {
            settings.javaScriptEnabled = true
            webViewClient = RegisterWebViewClient()
            webChromeClient = ProgressWebChromeClient(binding.progressBar)
            loadUrl(createAccountUrl, headers)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.webview_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.doneItem -> {
                finish()
                true
            }

            else -> false
        }
    }

    private inner class RegisterWebViewClient(
        activity: Activity = this@WebViewCreateAccountActivity,
        progressBar: ProgressBar = binding.progressBar,
        appUID: String = "",
    ) : LoginWebViewClient(activity, progressBar, appUID, createAccountUrl) {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url
            val host = url.host
            when (host) {
                successUrl -> {
                    successResult()
                }
                cancelUrl if ignoreFirstCancelUrl -> {
                    intent.putExtra(IGNORE_FIRST_CANCEL_URL_TAG, false)
                    if (isInfomaniakUrl(url.toString())) {
                        view.loadUrl(url.toString(), headers)
                    } else {
                        openUrl(url.toString())
                        cancelResult()
                    }
                }
                else -> {
                    openUrl(url.toString())
                }
            }
            return true
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            errorResult(error.description.toString())
        }

        private fun openUrl(url: String) = runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }

        private fun successResult() {
            setResult(RESULT_OK)
            finish()
        }

        private fun cancelResult() {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
