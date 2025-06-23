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
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.plus

/**
 * A [RecyclerView.ViewHolder] that has a new [currentBindScope] every time [RecyclerView.Adapter.onBindViewHolder] is called.
 *
 * This allows launching coroutines for a given element/position, that will auto-cancel when the ViewHolder is unbound.
 *
 * **IMPORTANT:** Make sure that a [parentScope] is passed (most versatile solution),
 * or that the host [RecyclerView.Adapter] implements [LifecycleOwner] AND is scoped to the entire life of this lifecycle
 * (i.e. isn't removed/added multiple times, pass the right [parentScope] otherwise).
 */
abstract class CoroutineScopeViewHolder<V : View>(
    view: V,
    private val parentScope: CoroutineScope? = null,
) : BindAwareViewHolder<V>(view) {

    private val currentBindScopeDelegate = ResettableLazy {
        val parentScope = parentScope
            ?: (bindingAdapter as? LifecycleOwner)?.lifecycleScope
            ?: error("Provide a parentScope in the constructor, or make the Adapter implement LifecycleOwner")
        parentScope + Job(parent = parentScope.coroutineContext.job)
    }

    val currentBindScope: CoroutineScope by currentBindScopeDelegate

    override fun onBind() = Unit

    override fun onUnbind() {
        currentBindScopeDelegate.value?.cancel()
        currentBindScopeDelegate.reset()
    }
}
