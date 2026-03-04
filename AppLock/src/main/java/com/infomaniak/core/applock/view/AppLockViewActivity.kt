/*
 * Infomaniak Authenticator - Android
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
package com.infomaniak.core.applock.view

import android.os.Bundle
import androidx.activity.addCallback
import com.infomaniak.core.applock.AppLockHelper.requestCredentials
import com.infomaniak.core.applock.BaseAppLockActivity
import com.infomaniak.core.applock.databinding.ActivityLockBinding
import com.infomaniak.core.common.extensions.appName
import kotlin.system.exitProcess

abstract class AppLockViewActivity : BaseAppLockActivity() {
    private val binding by lazy { ActivityLockBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) = with(binding) {
        super.onCreate(savedInstanceState)
        setContentView(root)

        onBackPressedDispatcher.addCallback(this@AppLockViewActivity) {
            finishAffinity()
            exitProcess(0)
        }

        unLock.apply {
            setBackgroundColor(primaryColor)
            setOnClickListener { requestCredentials { onCredentialsSuccessful() } }
        }
        imageViewTitle.contentDescription = appName
    }

    abstract val primaryColor: Int
}
