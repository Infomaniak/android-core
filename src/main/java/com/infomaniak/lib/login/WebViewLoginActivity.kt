package com.infomaniak.lib.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web_view_login.webview
import java.util.MissingFormatArgumentException

class WebViewLoginActivity : AppCompatActivity() {

	companion object {

		const val CLIENT_ID_TAG = "clientID"
		const val APPLICATION_ID_TAG = "appUID"
		const val CODE_TAG = "code"
		const val ERROR_TAG = "error"
	}

	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_web_view_login)

		val clientID = intent.extras?.getString(CLIENT_ID_TAG)
			?: throw MissingFormatArgumentException(CLIENT_ID_TAG)
		val appUID = intent.extras?.getString(APPLICATION_ID_TAG)
			?: throw MissingFormatArgumentException(CLIENT_ID_TAG)

		val infomaniakLogin = InfomaniakLogin(this, clientID = clientID, appUID = appUID)

		webview.apply {
			settings.javaScriptEnabled = true
			webViewClient = object : WebViewClient() {
				override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
					val url = request?.url.toString()
					return !isValidUrl(url)
				}

				override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
					return !isValidUrl(url)
				}

				override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
					handler?.proceed()
				}

				override fun onPageFinished(view: WebView?, url: String?) {
					val uri = Uri.parse(url)
					val scheme = uri.scheme
					if (scheme == appUID) {
						val intent = Intent().apply {
							putExtra("code", uri.getQueryParameter("code"))
							putExtra("error", uri.getQueryParameter("error"))
						}
						setResult(RESULT_OK, intent)
						finish()
					}
				}
			}
			loadUrl(infomaniakLogin.getUrl())
		}
	}

	private fun isValidUrl(url: String?): Boolean {
		if (url == null) return false
		return url.contains("login.infomaniak.com")
				|| url.contains("oauth2redirect")
				|| url.contains("www.google.com/recaptcha")
	}
}