/*
 * Infomaniak Core - Android
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
package com.infomaniak.lib.applock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.navArgs
import com.infomaniak.lib.applock.Utils.requestCredentials
import com.infomaniak.lib.applock.databinding.ActivityLockBinding
import com.infomaniak.lib.core.utils.getAppName
import java.util.Date
import kotlin.system.exitProcess

class LockActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLockBinding.inflate(layoutInflater) }
    private val lockViewModel: LockViewModel by viewModels()
    private val navigationArgs: LockActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) = with(binding) {
        super.onCreate(savedInstanceState)
        setContentView(root)
        if (lockViewModel.firstLaunch) {
            requestCredentials { onCredentialsSuccessful() }
        }

        onBackPressedDispatcher.addCallback(this@LockActivity) {
            finishAffinity()
            exitProcess(0)
        }

        unLock.apply {
            if (navigationArgs.primaryColor != UNDEFINED_PRIMARY_COLOR) setBackgroundColor(navigationArgs.primaryColor)
            setOnClickListener { requestCredentials { onCredentialsSuccessful() } }
        }
        imageViewTitle.contentDescription = getAppName()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lockViewModel.firstLaunch = false
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun onCredentialsSuccessful() = with(navigationArgs) {
        Log.i(Utils.APP_LOCK_TAG, "success")
        if (shouldStartActivity) {
            Intent(this@LockActivity, Class.forName(destinationClassName)).apply {
                destinationClassArgs?.let(::putExtras)
                startActivity(this)
            }
        }
        finish()
    }

    class LockViewModel : ViewModel() {
        var firstLaunch = true
    }

    companion object {
        private const val UNDEFINED_PRIMARY_COLOR = 0
        private const val SECURITY_APP_TOLERANCE = 1 * 60 * 1000 // 1min (ms)

        fun startAppLockActivity(
            context: Context,
            destinationClass: Class<*>,
            destinationClassArgs: Bundle? = null,
            primaryColor: Int = UNDEFINED_PRIMARY_COLOR,
            shouldStartActivity: Boolean = true,
        ) {
            val args = LockActivityArgs(destinationClass.name, primaryColor, shouldStartActivity, destinationClassArgs).toBundle()
            context.startActivity(Intent(context, LockActivity::class.java).putExtras(args))
        }

        fun lockAfterTimeout(
            context: Context,
            destinationClass: Class<*>,
            lastAppClosingTime: Long,
            primaryColor: Int = UNDEFINED_PRIMARY_COLOR,
            securityTolerance: Int = SECURITY_APP_TOLERANCE,
        ) {
            val lastCloseAppWithTolerance = Date(lastAppClosingTime + securityTolerance)
            if (Date().after(lastCloseAppWithTolerance)) {
                startAppLockActivity(context, destinationClass, primaryColor = primaryColor, shouldStartActivity = false)
            }
        }
    }
}
