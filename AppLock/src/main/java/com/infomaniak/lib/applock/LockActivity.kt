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

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.infomaniak.lib.applock.databinding.ActivityLockBinding
import com.infomaniak.lib.core.utils.getAppName
import com.infomaniak.lib.core.utils.requestCredentials

class LockActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLockBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) = with(binding) {
        super.onCreate(savedInstanceState)
        setContentView(root)
        if (savedInstanceState?.getBoolean("firstLaunch") != false) {
            requestCredentials { onCredentialsSuccessful() }
        }
        imageViewTitle.contentDescription = getAppName()
        unLock.setOnClickListener { requestCredentials { onCredentialsSuccessful() } }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("firstLaunch", false)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun onCredentialsSuccessful() {
        Log.i(FACE_ID_LOG_TAG, "success")
        setResult(RESULT_OK)
        finish()
    }

    companion object {
        const val FACE_ID_LOG_TAG = "Face ID"
    }
}
