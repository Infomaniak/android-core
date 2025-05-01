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

@file:Suppress("PackageDirectoryMismatch")

package androidx.recyclerview.widget

import android.view.View

abstract class BindAwareViewHolder<V : View>(
    @JvmField val view: V
) : RecyclerView.ViewHolder(view) {

    @Deprecated(
        message = "Use view instead",
        replaceWith = ReplaceWith("view"),
        level = DeprecationLevel.ERROR
    )
    inline val itemViewWithType: V get() = view

    protected open fun onBind() = Unit

    protected open fun onUnbind() = Unit

    internal final override fun setFlags(flags: Int, mask: Int) {
        val wasBound = isBound
        super.setFlags(flags, mask)
        notifyBinding(wasBound, isBound)
    }

    internal final override fun addFlags(flags: Int) {
        val wasBound = isBound
        super.addFlags(flags)
        notifyBinding(wasBound, isBound)
    }

    internal final override fun clearPayload() {
        val wasBound = isBound
        super.clearPayload()
        notifyBinding(wasBound, isBound)
    }

    internal final override fun resetInternal() {
        val wasBound = isBound
        super.resetInternal()
        notifyBinding(wasBound, isBound)
    }

    private fun notifyBinding(previousBound: Boolean, currentBound: Boolean) {
        if (previousBound && !currentBound) {
            onUnbind()
        } else if (!previousBound && currentBound) {
            onBind()
        }
    }
}
