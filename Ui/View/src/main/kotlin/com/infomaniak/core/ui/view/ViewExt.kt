/*
 * Infomaniak Core - Android
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
package com.infomaniak.core.ui.view

import android.view.View
import android.view.ViewGroup

fun View.setMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        setMargins(
            left ?: leftMargin,
            top ?: topMargin,
            right ?: rightMargin,
            bottom ?: bottomMargin,
        )
        requestLayout()
    }
}

fun View.setMarginsRelative(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        start?.let { marginStart = it }
        top?.let { topMargin = it }
        end?.let { marginEnd = it }
        bottom?.let { bottomMargin = it }
        requestLayout()
    }
}

fun View.setPaddingRelative(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        setPaddingRelative(
            start ?: paddingStart,
            top ?: paddingTop,
            end ?: paddingEnd,
            bottom ?: paddingBottom,
        )
        requestLayout()
    }
}
