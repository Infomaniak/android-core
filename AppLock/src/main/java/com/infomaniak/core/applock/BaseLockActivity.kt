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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.navArgs
import com.infomaniak.core.applock.LockManager.unlock
import com.infomaniak.core.applock.Utils.requestCredentials
import com.infomaniak.core.applock.view.LockViewActivityArgs
import android.R as RAndroid

abstract class BaseLockActivity : FragmentActivity() {
    private val lockViewModel: LockViewModel by viewModels()
    private val navigationArgs: LockViewActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (lockViewModel.firstLaunch) {
            requestCredentials { onCredentialsSuccessful() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lockViewModel.firstLaunch = false
    }

    // TODO: Fix deprecated
    override fun onPause() {
        super.onPause()
        overridePendingTransition(RAndroid.anim.fade_in, RAndroid.anim.fade_out)
    }

    protected fun onCredentialsSuccessful() = with(navigationArgs) {
        Log.i(Utils.APP_LOCK_TAG, "success")
        unlock()
        if (shouldStartActivity) {
            Intent(this@BaseLockActivity, Class.forName(destinationClassName)).apply {
                destinationClassArgs?.let(::putExtras)
            }.also(::startActivity)
        }
        finish()
    }

    class LockViewModel : ViewModel() {
        var firstLaunch = true
    }
}
