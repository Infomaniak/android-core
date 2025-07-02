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
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import com.infomaniak.core.compose.materialthemefromxml.MaterialThemeFromXml
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.utils.getColorOrNull
import com.infomaniak.core.crossloginui.views.components.CrossLoginSelectAccounts

class CrossLoginSelectAccountsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val accounts = mutableStateListOf<CrossLoginUiAccount>()

    private var onClickListener: (() -> Unit)? = null

    private var primaryColor by mutableStateOf<Color?>(null)
    private var onPrimaryColor by mutableStateOf<Color?>(null) // TODO
    private var titleColor: Color? = null
    private var descriptionColor: Color? = null
    private var avatarStrokeColor: Color? = null
    private var buttonStrokeColor: Color? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CrossLoginSelectAccountsView, defStyleAttr, 0).apply {
            primaryColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginPrimaryColor)
            onPrimaryColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginOnPrimaryColor)
            titleColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginTitleColor)
            descriptionColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginDescriptionColor)
            avatarStrokeColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginAvatarStrokeColor)
            buttonStrokeColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginButtonStrokeColor)
            recycle()
        }
    }

    @Composable
    override fun Content() {
        MaterialThemeFromXml {

            val colors = CrossLoginDefaults.colors(
                primary = primaryColor,
                onPrimary = onPrimaryColor,
                title = titleColor,
                description = descriptionColor,
                avatarStroke = avatarStrokeColor,
                buttonStroke = buttonStrokeColor,
            )

            CrossLoginSelectAccounts(
                accounts = { accounts },
                colors = colors,
                onClick = { onClickListener?.invoke() },
            )
        }
    }

    fun setPrimaryColor(@ColorInt newPrimaryColor: Int) {
        primaryColor = Color(newPrimaryColor)
    }

    fun setOnPrimaryColor(@ColorInt newOnPrimaryColor: Int) {
        onPrimaryColor = Color(newOnPrimaryColor)
    }

    fun setAccounts(list: List<CrossLoginUiAccount>) {
        accounts.apply {
            clear()
            addAll(list)
        }
    }

    fun setOnClickListener(listener: () -> Unit) {
        onClickListener = listener
    }

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Use the other methods to set click listeners when a reaction is clicked and when the add reaction button is clicked")
    override fun setOnClickListener(listener: OnClickListener?) = super.setOnClickListener(listener)
}
