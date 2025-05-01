/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
@file:OptIn(DelicateCoroutinesApi::class)

package com.infomaniak.core.recyclerview

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.BindAwareViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.plus

abstract class CoroutineScopeViewHolder<V : View>(view: V) : BindAwareViewHolder<V>(view) {

    private val currentBindScopeDelegate = ResettableLazy {
        val parentScope = (bindingAdapter as LifecycleOwner).lifecycleScope
        parentScope + Job(parent = parentScope.coroutineContext.job)
    }

    var currentBindScope: CoroutineScope by currentBindScopeDelegate

    override fun onBind() = Unit

    override fun onUnbind() {
        currentBindScope.cancel()
        currentBindScopeDelegate.invalidate()
    }
}
