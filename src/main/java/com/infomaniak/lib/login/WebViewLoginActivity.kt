package com.infomaniak.lib.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.infomaniak.lib.login.InfomaniakLogin.Companion.LOGIN_URL_TAG
import com.infomaniak.lib.login.InfomaniakLogin.Companion.REMOVE_COOKIES_TAG
import com.infomaniak.lib.login.databinding.ActivityWebViewLoginBinding
import java.util.MissingFormatArgumentException

class WebViewLoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityWebViewLoginBinding.inflate(layoutInflater) }

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
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        if (removeCookies) WebViewUtils.flushAllCookies()

        binding.webview.apply {
            settings.javaScriptEnabled = true
            webViewClient = LoginWebViewClient(
                activity = this@WebViewLoginActivity,
                progressBar = binding.progressBar,
                appUID = appUID,
                baseUrl = loginUrl,
            )
            webChromeClient = ProgressWebChromeClient(binding.progressBar)
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

    companion object {

        const val APPLICATION_ID_TAG = "appUID"
    }
}
