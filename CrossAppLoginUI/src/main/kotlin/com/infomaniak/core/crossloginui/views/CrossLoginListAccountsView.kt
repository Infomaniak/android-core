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
import com.infomaniak.core.crossloginui.views.components.CrossLoginListAccounts

class CrossLoginListAccountsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val accounts = mutableStateListOf<CrossLoginUiAccount>()

    private var onAccountClickedListener: ((account: CrossLoginUiAccount) -> Unit)? = null
    private var onAnotherAccountClickedListener: (() -> Unit)? = null
    private var onCloseClicked: (() -> Unit)? = null

    private var primaryColor by mutableStateOf<Color?>(null)
    private var onPrimaryColor by mutableStateOf<Color?>(null)
    private var titleColor: Color?
    private var descriptionColor: Color?
    private var avatarStrokeColor: Color?
    private var buttonStrokeColor: Color?

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CrossLoginListAccountsView, defStyleAttr, 0).apply {
            primaryColor = getColorOrNull(R.styleable.CrossLoginListAccountsView_crossLoginPrimaryColor)
            onPrimaryColor = getColorOrNull(R.styleable.CrossLoginListAccountsView_crossLoginOnPrimaryColor)
            titleColor = getColorOrNull(R.styleable.CrossLoginListAccountsView_crossLoginTitleColor)
            descriptionColor = getColorOrNull(R.styleable.CrossLoginListAccountsView_crossLoginDescriptionColor)
            avatarStrokeColor = getColorOrNull(R.styleable.CrossLoginListAccountsView_crossLoginAvatarStrokeColor)
            buttonStrokeColor = getColorOrNull(R.styleable.CrossLoginListAccountsView_crossLoginButtonStrokeColor)
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

            CrossLoginListAccounts(
                accounts = { accounts },
                colors = colors,
                onAccountClicked = { onAccountClickedListener?.invoke(it) },
                onAnotherAccountClicked = { onAnotherAccountClickedListener?.invoke() },
                onCloseClicked = { onCloseClicked?.invoke() },
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

    fun setOnAccountClickedListener(listener: (CrossLoginUiAccount) -> Unit) {
        onAccountClickedListener = listener
    }

    fun setOnAnotherAccountClickedListener(listener: () -> Unit) {
        onAnotherAccountClickedListener = listener
    }

    fun setOnCloseClickedListener(listener: () -> Unit) {
        onCloseClicked = listener
    }
}
