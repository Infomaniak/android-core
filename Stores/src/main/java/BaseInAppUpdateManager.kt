/*
 * Infomaniak kDrive - Android
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
package com.infomaniak.lib.stores

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

abstract class BaseInAppUpdateManager(activity: FragmentActivity) : DefaultLifecycleObserver {

    var onInAppUpdateUiChange: ((Boolean) -> Unit)? = null

    protected val localSettings = StoresLocalSettings.getInstance(activity)

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        localSettings.appUpdateLaunches++
        handleUpdates()
    }

    abstract fun installDownloadedUpdate()

    protected abstract fun checkUpdateIsAvailable()

    private fun handleUpdates() = with(localSettings) {
        if (appUpdateLaunches != 0 && (isUserWantingUpdates || appUpdateLaunches % 10 == 0)) checkUpdateIsAvailable()
    }
}
