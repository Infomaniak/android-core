/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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
package com.infomaniak.lib.core.views

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class DividerItemDecorator(
    private val divider: Drawable,
    private val shouldIgnoreView: ((View) -> Boolean)? = null,
) : RecyclerView.ItemDecoration() {

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.childCount == 0) return

        val firstChildY = parent.children.minOf { it.y }
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight

        // Don't ever try to use index of children again… They are not in order :(
        parent.children.forEach { child ->
            if (child.y == firstChildY || shouldIgnoreView?.invoke(child) == true) return@forEach

            val yTranslation = child.translationY.roundToInt()
            val dividerTop = child.top - (child.layoutParams as RecyclerView.LayoutParams).topMargin + yTranslation
            val dividerBottom = dividerTop + divider.intrinsicHeight
            divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            divider.draw(canvas)
        }
    }
}
