package com.infomaniak.lib.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.infomaniak.lib.login.InfomaniakLogin.Companion.LOGIN_URL_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.REMOVE_COOKIES_TAG
import kotlinx.android.synthetic.main.activity_web_view_login.*
import java.util.*

class WebViewLoginActivity : AppCompatActivity() {

    private val appUID: String by lazy {
        intent.getStringExtra(APPLICATION_ID_TAG) ?: throw MissingFormatArgumentException(APPLICATION_ID_TAG)
    }
    private val loginUrl: String by lazy {
        intent.getStringExtra(LOGIN_URL_TAG) ?: throw MissingFormatArgumentException(LOGIN_URL_TAG)
    }
    private val removeCookies: Boolean by lazy {
        intent.getBooleanExtra(REMOVE_COOKIES_TAG, true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_login)
        setSupportActionBar(toolbar)

        if (removeCookies) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }

        webview.apply {
            settings.javaScriptEnabled = true
            webViewClient = LoginWebViewClient()
            webChromeClient = ProgressWebChromeClient()
            loadUrl(loginUrl)
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

    private fun isValidUrl(url: String?): Boolean {
        if (url == null) return false
        if (onAuthResponse(Uri.parse(url))) return false
        return url.contains("login.infomaniak.com")
                || url.contains("oauth2redirect")
                || url.contains("gstatic.com")
                || url.contains("google.com/recaptcha")
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

    private fun successResult(code: String) {
        val intent = Intent().apply {
            putExtra(InfomaniakLogin.CODE_TAG, code)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun errorResult(errorCode: String) {
        val intent = Intent().apply {
            putExtra(InfomaniakLogin.ERROR_CODE_TAG, errorCode)
            putExtra(InfomaniakLogin.ERROR_TRANSLATED_TAG, translateError(errorCode))
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun translateError(errorCode: String): String {
        return when (errorCode) {
            InfomaniakLogin.WEBVIEW_ERROR_CODE_INTERNET_DISCONNECTED -> getString(R.string.connection_error)
            InfomaniakLogin.WEBVIEW_ERROR_CODE_CONNECTION_REFUSED -> getString(R.string.connection_error)
            InfomaniakLogin.ERROR_ACCESS_DENIED -> getString(R.string.access_denied)
            else -> getString(R.string.an_error_has_occurred)
        }
    }

    private inner class LoginWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url.toString()
            return !isValidUrl(url)
        }

        @Deprecated("Only for API below 23")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return !isValidUrl(url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progressBar.progress = 0
            progressBar.isVisible = true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            progressBar.progress = 100
            progressBar.isGone = true
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            error?.certificate?.apply {
                if (issuedBy?.cName == "localhost" && issuedTo?.cName == "localhost") return
            }
            errorResult(InfomaniakLogin.SSL_ERROR_CODE)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            if (isValidUrl(request?.url.toString())) {
                errorResult(error?.description?.toString() ?: "")
            }
        }

        @Deprecated("Only for API below 23")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            if (isValidUrl(failingUrl)) {
                errorResult(description ?: "")
            }
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            if (request?.method == "GET") errorResult(InfomaniakLogin.HTTP_ERROR_CODE)
        }
    }

    private inner class ProgressWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            progressBar.progress = newProgress
            if (newProgress == 100) {
                progressBar.isGone = true
            }
        }
    }

    companion object {

        const val APPLICATION_ID_TAG = "appUID"
    }
}
