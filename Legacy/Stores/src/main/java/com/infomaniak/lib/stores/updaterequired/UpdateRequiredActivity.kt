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
package com.infomaniak.lib.stores.updaterequired

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.infomaniak.lib.core.utils.clearStack
import com.infomaniak.lib.core.utils.showToast
import com.infomaniak.lib.stores.StoreUtils.goToPlayStore
import com.infomaniak.lib.stores.databinding.ActivityUpdateRequiredBinding
import com.infomaniak.lib.stores.updatemanagers.InAppUpdateManager
import kotlin.system.exitProcess
import com.infomaniak.lib.core.R as RCore

class UpdateRequiredActivity : AppCompatActivity() {

    private val binding by lazy { ActivityUpdateRequiredBinding.inflate(layoutInflater) }
    private val navigationArgs: UpdateRequiredActivityArgs by navArgs()

    private val inAppUpdateManager by lazy { InAppUpdateManager(this, navigationArgs.appId, navigationArgs.versionCode) }

    override fun onCreate(savedInstanceState: Bundle?): Unit = with(binding) {
        super.onCreate(savedInstanceState)
        setTheme(navigationArgs.appTheme)
        setContentView(root)

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

        updateAppButton.apply {
            val primaryColor = getPrimaryColor()
            if (primaryColor != UNDEFINED_PRIMARY_COLOR) setBackgroundColor(primaryColor)
            setOnClickListener { inAppUpdateManager.requireUpdate() }
        }
    }

    private fun MaterialButton.getPrimaryColor() = if (SDK_INT >= 23) {
        MaterialColors.getColor(ContextThemeWrapper(context, theme), R.attr.colorPrimary, UNDEFINED_PRIMARY_COLOR)
    } else {
        UNDEFINED_PRIMARY_COLOR
    }

    companion object {
        private const val UNDEFINED_PRIMARY_COLOR = 0

        fun startUpdateRequiredActivity(context: Context, appId: String, versionCode: Int, appTheme: Int) {
            Intent(context, UpdateRequiredActivity::class.java).apply {
                val args = UpdateRequiredActivityArgs(appId, versionCode, appTheme)
                putExtras(args.toBundle())
                clearStack()
            }.also(context::startActivity)
        }
    }
}
