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
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import com.infomaniak.core.extensions.clearStack
import com.infomaniak.core.extensions.goToPlayStore
import com.infomaniak.core.extensions.showToast
import com.infomaniak.core.inappstore.BuildConfig
import com.infomaniak.core.inappstore.R
import com.infomaniak.core.inappstore.ui.screen.UpdateRequired
import com.infomaniak.core.inappstore.ui.theme.InAppStoreXMLTheme
import com.infomaniak.inappstore.updatemanagers.InAppUpdateManager
import kotlin.system.exitProcess

class UpdateRequiredActivity : ComponentActivity() {

    private val appId by lazy { intent.getStringExtra(EXTRA_APP_ID) ?: "" }
    private val versionCode by lazy { intent.getIntExtra(EXTRA_VERSION_CODE, 0) }
    private val appIllustration by lazy { intent.getIntExtra(EXTRA_APP_ILLUSTRATION, -1) }

    private val inAppUpdateManager by lazy { InAppUpdateManager(this, appId, versionCode) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        inAppUpdateManager.init(
            mustRequireImmediateUpdate = true,
            onInstallFailure = {
                showToast(R.string.errorUpdateInstall)
                goToPlayStore()
            },
        )

        setContent {
            InAppStoreXMLTheme {
                UpdateRequired(
                    onBack = {
                        finishAffinity()
                        exitProcess(0)
                    },
                    onInstallButtonClicked = {
                        inAppUpdateManager.requireUpdate {
                            if (BuildConfig.DEBUG) {
                                // The appended `.debug` to the packageName in debug mode should be removed if we want to test this.
                                goToPlayStore("com.infomaniak.swisstransfer")
                            } else {
                                goToPlayStore()
                            }
                        }
                    },
                    appIllustration
                )
            }
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

    companion object {
        private const val EXTRA_APP_ID = "EXTRA_APP_ID"
        private const val EXTRA_VERSION_CODE = "EXTRA_VERSION_CODE"
        private const val EXTRA_APP_ILLUSTRATION = "EXTRA_APP_ILLUSTRATION"

        fun startUpdateRequiredActivity(
            context: Context,
            appId: String,
            versionCode: Int,
            @DrawableRes appIllustration: Int,
        ) {
            Intent(context, UpdateRequiredActivity::class.java).apply {
                putExtra(EXTRA_APP_ID, appId)
                putExtra(EXTRA_VERSION_CODE, versionCode)
                putExtra(EXTRA_APP_ILLUSTRATION, appIllustration)

                clearStack()
            }.also(context::startActivity)
        }
    }
}
