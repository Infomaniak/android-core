/*
 * Infomaniak Mail - Android
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
package com.infomaniak.core.crossloginui.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.AbstractComposeView
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.views.components.CrossLoginSelectAccounts

class CrossLoginSelectAccountsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val accounts = mutableStateListOf<CrossLoginUiAccount>()

    private var onClickListener: (() -> Unit)? = null

    // TODO
    // init {
    //     context.obtainStyledAttributes(attrs, R.styleable.CrossLoginSelectAccountsView, defStyleAttr, 0).apply {
    //         chipCornerRadius = getDimensionOrNull(R.styleable.CrossLoginSelectAccountsView_chipCornerRadius)
    //         addReactionIconRes = getResourceIdOrNull(R.styleable.CrossLoginSelectAccountsView_addReactionIcon)
    //         addReactionDisabledColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_addReactionDisabledColor)
    //         recycle()
    //     }
    // }

    private fun TypedArray.getDimensionOrNull(@StyleableRes index: Int): Float? {
        return if (hasValue(index)) getDimension(index, -1f) else null
    }

    private fun TypedArray.getResourceIdOrNull(@StyleableRes index: Int): Int? {
        return if (hasValue(index)) getResourceId(index, -1) else null
    }

    @ColorInt
    private fun TypedArray.getColorOrNull(@StyleableRes index: Int): Int? {
        return if (hasValue(index)) getColor(index, -1) else null
    }

    @Composable
    override fun Content() {
        CrossLoginSelectAccounts(
            accounts = { accounts },
            onClick = { onClickListener?.invoke() },
        )
    }

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Use the other methods to set click listeners when a reaction is clicked and when the add reaction button is clicked")
    override fun setOnClickListener(listener: OnClickListener?) = super.setOnClickListener(listener)

    fun setAccounts(list: List<CrossLoginUiAccount>) {
        accounts.apply {
            clear()
            addAll(list)
        }
    }

    fun setOnClickListener(listener: () -> Unit) {
        onClickListener = listener
    }
}
