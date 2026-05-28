/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.infomaniak.core.login.InfomaniakLogin.Companion.LOGIN_URL_TAG
import com.infomaniak.core.login.InfomaniakLogin.Companion.REMOVE_COOKIES_TAG
import com.infomaniak.lib.login.databinding.ActivityWebViewLoginBinding
import com.infomaniak.core.login.ext.handleEdgeToEdge
import com.infomaniak.lib.login.R

class WebViewLoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityWebViewLoginBinding.inflate(layoutInflater) }

    private val appUID: String by lazy {
        intent.getStringExtra(APPLICATION_ID_TAG) ?: throw IllegalArgumentException(APPLICATION_ID_TAG)
    }
    private val loginUrl: String by lazy {
        intent.getStringExtra(LOGIN_URL_TAG) ?: throw IllegalArgumentException(LOGIN_URL_TAG)
    }
    private val removeCookies: Boolean by lazy {
        intent.getBooleanExtra(REMOVE_COOKIES_TAG, true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        enableEdgeToEdge()

        if (removeCookies) WebViewUtils.flushAllCookies()
        binding.handleEdgeToEdge()

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
