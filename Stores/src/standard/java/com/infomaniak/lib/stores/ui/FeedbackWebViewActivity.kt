/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.lib.stores.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.infomaniak.lib.stores.databinding.ActivityFeedbackWebviewBinding

class FeedbackWebViewActivity : AppCompatActivity() {

    private val binding by lazy { ActivityFeedbackWebviewBinding.inflate(layoutInflater) }
    private val navArgs: FeedbackWebViewActivityArgs by navArgs()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.webview.apply {
            settings.javaScriptEnabled = true
            loadUrl(navArgs.url)
        }
    }

    companion object {

        fun startActivity(context: Context, url: String) {
            Intent(context, FeedbackWebViewActivity::class.java).apply {
                putExtras(FeedbackWebViewActivityArgs(url).toBundle())
            }.also(context::startActivity)
        }
    }
}
