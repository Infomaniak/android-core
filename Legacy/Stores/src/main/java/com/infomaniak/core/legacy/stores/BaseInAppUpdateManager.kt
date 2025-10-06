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
package com.infomaniak.core.legacy.stores

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.infomaniak.core.legacy.stores.StoreUtils.goToAppStore

abstract class BaseInAppUpdateManager(private val activity: FragmentActivity) : DefaultLifecycleObserver {

    protected var onInAppUpdateUiChange: ((Boolean) -> Unit)? = null
    protected var onFDroidResult: ((Boolean) -> Unit)? = null

    protected val viewModel: StoresViewModel by lazy { ViewModelProvider(activity)[StoresViewModel::class.java] }

    open fun installDownloadedUpdate() = Unit

    open fun requireUpdate(onFailure: ((Exception) -> Unit)? = null) = activity.goToAppStore()

    open fun init(
        mustRequireImmediateUpdate: Boolean = false,
        onUserChoice: ((Boolean) -> Unit)? = null,
        onInstallStart: (() -> Unit)? = null,
        onInstallFailure: ((Exception) -> Unit)? = null,
        onInstallSuccess: (() -> Unit)? = null,
        onInAppUpdateUiChange: ((Boolean) -> Unit)? = null,
        onFDroidResult: ((Boolean) -> Unit)? = null,
    ) = init(mustRequireImmediateUpdate, onInAppUpdateUiChange, onFDroidResult)

    protected fun init(
        mustRequireImmediateUpdate: Boolean = false,
        onInAppUpdateUiChange: ((Boolean) -> Unit)? = null,
        onFDroidResult: ((Boolean) -> Unit)? = null,
    ) {
        this.onInAppUpdateUiChange = onInAppUpdateUiChange
        this.onFDroidResult = onFDroidResult

        if (!mustRequireImmediateUpdate) activity.lifecycle.addObserver(observer = this)
    }

    protected abstract fun checkUpdateIsAvailable()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        with(viewModel) {
            if (!viewModel.isUpdateBottomSheetShown) shouldCheckUpdate(::checkUpdateIsAvailable)
            decrementAppUpdateLaunches()
        }
    }
}
