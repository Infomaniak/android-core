/*
 * Infomaniak Login - Android
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

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.infomaniak.lib.login.InfomaniakLogin.Companion.CODE_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.ERROR_ACCESS_DENIED
import com.infomaniak.lib.login.InfomaniakLogin.Companion.ERROR_CODE_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.ERROR_TRANSLATED_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.HTTP_ERROR_CODE
import com.infomaniak.lib.login.InfomaniakLogin.Companion.SSL_ERROR_CODE
import com.infomaniak.lib.login.InfomaniakLogin.Companion.WEBVIEW_ERROR_CODE_CONNECTION_REFUSED
import com.infomaniak.lib.login.InfomaniakLogin.Companion.WEBVIEW_ERROR_CODE_INTERNET_DISCONNECTED
import com.infomaniak.lib.login.InfomaniakLogin.Companion.WEBVIEW_ERROR_CODE_NAME_NOT_RESOLVED
import okhttp3.HttpUrl.Companion.toHttpUrl

private val INFOMANIAK_REGEX = Regex(".*\\.infomaniak\\.(com|ch)")

open class LoginWebViewClient(
    private val activity: Activity,
    private val progressBar: ProgressBar,
    private val appUID: String,
    private val baseUrl: String,
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        progressBar.progress = 0
        progressBar.isVisible = true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        progressBar.progress = 100
        progressBar.isGone = true
    }

    @Deprecated("Support API below 24")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return !isValidUrl(url)
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        error?.certificate?.apply {
            if (issuedBy?.cName == "localhost" && issuedTo?.cName == "localhost") return
        }
        errorResult(SSL_ERROR_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError) {
        onReceivedError(view, error.errorCode, error.description.toString(), request?.url.toString())
    }

    @Suppress("OVERRIDE_DEPRECATION") // We have this overload for api 22 and below
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String, failingUrl: String) {
        if (isValidUrl(failingUrl)) errorResult(description)
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        if (isValidUrl(request?.url.toString())) errorResult(HTTP_ERROR_CODE)
    }

    private fun isValidUrl(inputUrl: String?): Boolean {
        if (inputUrl == null || onAuthResponse(Uri.parse(inputUrl))) return false
        val baseUrlHost = baseUrl.toHttpUrl().host
        val inputUrlHost = inputUrl.toHttpUrl().host

        return inputUrlHost == baseUrlHost
                || inputUrl.contains("oauth2redirect")
                || !inputUrlHost.contains(INFOMANIAK_REGEX) // Allow all other redirects except unmanaged Infomaniak redirects
    }

    private fun onAuthResponse(uri: Uri?): Boolean {
        return if (uri?.scheme == appUID) {
            uri.getQueryParameter("code")?.let { code ->
                successResult(code)
            } ?: run {
                errorResult(uri.getQueryParameter("error") ?: "")
            }
            true
        } else false
    }

    private fun successResult(code: String) = with(activity) {
        val intent = Intent().apply {
            putExtra(CODE_TAG, code)
        }
        setResult(AppCompatActivity.RESULT_OK, intent)
        finish()
    }

    protected fun errorResult(errorCode: String) = with(activity) {
        val intent = Intent().apply {
            putExtra(ERROR_CODE_TAG, errorCode)
            putExtra(ERROR_TRANSLATED_TAG, translateError(errorCode))
        }
        setResult(AppCompatActivity.RESULT_OK, intent)
        finish()
    }

    private fun translateError(errorCode: String): String = with(activity) {
        return when (errorCode) {
            WEBVIEW_ERROR_CODE_INTERNET_DISCONNECTED,
            WEBVIEW_ERROR_CODE_CONNECTION_REFUSED,
            WEBVIEW_ERROR_CODE_NAME_NOT_RESOLVED -> getString(R.string.connection_error)

            ERROR_ACCESS_DENIED -> getString(R.string.access_denied)
            else -> getString(R.string.an_error_has_occurred)
        }
    }
}
