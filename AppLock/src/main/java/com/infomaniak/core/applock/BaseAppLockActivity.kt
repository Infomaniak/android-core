/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2026 Infomaniak Network SA
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
package com.infomaniak.core.applock

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.infomaniak.core.applock.AppLockHelper.requestCredentials
import com.infomaniak.core.applock.AppLockManager.unlock
import android.R as RAndroid

abstract class BaseAppLockActivity : FragmentActivity() {
    private val lockViewModel: LockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        if (SDK_INT >= 29) window.isNavigationBarContrastEnforced = false

        if (lockViewModel.firstLaunch) {
            requestCredentials { onCredentialsSuccessful() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lockViewModel.firstLaunch = false
    }

    override fun onPause() {
        super.onPause()
        if (SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN, RAndroid.anim.fade_in, RAndroid.anim.fade_out
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(RAndroid.anim.fade_in, RAndroid.anim.fade_out)
        }
    }

    protected fun onCredentialsSuccessful() {
        Log.i(AppLockHelper.APP_LOCK_TAG, "success")
        unlock()
        finish()
    }

    class LockViewModel : ViewModel() {
        var firstLaunch = true
    }
}
