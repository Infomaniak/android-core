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
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import com.infomaniak.core.compose.basics.ButtonType
import com.infomaniak.core.compose.materialthemefromxml.MaterialThemeFromXml
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.data.CrossLoginCustomization
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.utils.getColorOrNull
import com.infomaniak.core.crossloginui.utils.getStringOrNull
import com.infomaniak.core.crossloginui.views.components.CrossLoginSelectAccounts
import com.infomaniak.core.utils.enumValueOfOrNull

class CrossLoginSelectAccountsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val accounts = mutableStateListOf<CrossLoginUiAccount>()
    private val selectedIds = mutableStateSetOf<Int>()

    private var onClickListener: OnClickListener? = null

    private var primaryColor by mutableStateOf<Color?>(null)
    private var onPrimaryColor by mutableStateOf<Color?>(null)
    private var titleColor: Color?
    private var descriptionColor: Color?
    private var avatarStrokeColor: Color?
    private var buttonStrokeColor: Color?
    private var buttonType: ButtonType?

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CrossLoginSelectAccountsView, defStyleAttr, 0).apply {
            primaryColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginPrimaryColor)
            onPrimaryColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginOnPrimaryColor)
            titleColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginTitleColor)
            descriptionColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginDescriptionColor)
            avatarStrokeColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginAvatarStrokeColor)
            buttonStrokeColor = getColorOrNull(R.styleable.CrossLoginSelectAccountsView_crossLoginButtonStrokeColor)
            val crossLoginButtonType = getStringOrNull(R.styleable.CrossLoginListAccountsView_crossLoginButtonType)
            buttonType = enumValueOfOrNull<ButtonType>(crossLoginButtonType)

            recycle()
        }
    }

    @Composable
    override fun Content() {
        MaterialThemeFromXml {

            val customization = CrossLoginCustomization(
                colors = CrossLoginDefaults.colors(
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    titleColor = titleColor,
                    descriptionColor = descriptionColor,
                    avatarStrokeColor = avatarStrokeColor,
                    buttonStrokeColor = buttonStrokeColor,
                ),
                buttonType = CrossLoginDefaults.buttonType(buttonType),
            )

            CrossLoginSelectAccounts(
                accounts = { accounts },
                selectedIds = { selectedIds },
                customization = customization,
                onClick = { onClickListener?.onClick(this) },
            )
        }
    }

    fun setPrimaryColor(@ColorInt newPrimaryColor: Int) {
        primaryColor = Color(newPrimaryColor)
    }

    fun setOnPrimaryColor(@ColorInt newOnPrimaryColor: Int) {
        onPrimaryColor = Color(newOnPrimaryColor)
    }

    fun setAccounts(items: List<CrossLoginUiAccount>) {
        accounts.apply {
            clear()
            addAll(items)
        }
    }

    fun setSelectedIds(items: Set<Int>) {
        selectedIds.apply {
            clear()
            addAll(items)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }
}
