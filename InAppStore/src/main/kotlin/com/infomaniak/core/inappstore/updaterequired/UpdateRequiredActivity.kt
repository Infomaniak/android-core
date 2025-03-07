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
package com.infomaniak.core.inappstore.updaterequired

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.infomaniak.core.extensions.clearStack
import com.infomaniak.core.extensions.goToPlayStore
import com.infomaniak.core.extensions.showToast
import com.infomaniak.core.inappstore.R
import com.infomaniak.core.inappstore.ui.screen.UpdateRequired
import com.infomaniak.core.inappstore.ui.theme.InAppStoreTheme
import com.infomaniak.core.ui.theme.Margin
import com.infomaniak.inappstore.updatemanagers.InAppUpdateManager
import kotlin.system.exitProcess
import com.google.android.material.R as RMaterial

class UpdateRequiredActivity : ComponentActivity() {

    private val appId = intent.getStringExtra(EXTRA_APP_ID) ?: ""
    private val versionCode = intent.getIntExtra(EXTRA_VERSION_CODE, 0)

    private val inAppUpdateManager by lazy { InAppUpdateManager(this, appId, versionCode) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LaunchedEffect(Unit) {
                inAppUpdateManager.init(
                    mustRequireImmediateUpdate = true,
                    onInstallFailure = {
                        showToast(R.string.errorUpdateInstall)
                        goToPlayStore()
                    },
                )
            }

            UpdateRequired(
                onBack = {
                    finishAffinity()
                    exitProcess(0)
                },
            )
        }

        /*
        inAppUpdateManager.init(
            mustRequireImmediateUpdate = true,
            onInstallFailure = {
                showToast(RCore.string.errorUpdateInstall)
                goToPlayStore()
            },
        )

        onBackPressedDispatcher.addCallback(this@UpdateRequiredActivity) {
            finishAffinity()
            exitProcess(0)
        }
        */

        // TODO:
        /*
        updateAppButton.apply {
            val primaryColor = getPrimaryColor()
            if (primaryColor != UNDEFINED_PRIMARY_COLOR) setBackgroundColor(primaryColor)
            setOnClickListener { inAppUpdateManager.requireUpdate() }
        }
        */
    }

    private fun MaterialButton.getPrimaryColor() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        MaterialColors.getColor(ContextThemeWrapper(context, theme), RMaterial.attr.colorPrimary, UNDEFINED_PRIMARY_COLOR)
    } else {
        UNDEFINED_PRIMARY_COLOR
    }

    companion object {
        private const val EXTRA_APP_ID = "EXTRA_APP_ID"
        private const val EXTRA_VERSION_CODE = "EXTRA_VERSION_CODE"
        private const val EXTRA_APP_THEME = "EXTRA_APP_THEME"

        private const val UNDEFINED_PRIMARY_COLOR = 0

        fun startUpdateRequiredActivity(context: Context, appId: String, versionCode: Int, appTheme: Int) {
            Intent(context, UpdateRequiredActivity::class.java).apply {
                putExtra(EXTRA_APP_ID, appId)
                putExtra(EXTRA_VERSION_CODE, versionCode)
                putExtra(EXTRA_APP_THEME, appTheme)

                clearStack()
            }.also(context::startActivity)
        }
    }
}
