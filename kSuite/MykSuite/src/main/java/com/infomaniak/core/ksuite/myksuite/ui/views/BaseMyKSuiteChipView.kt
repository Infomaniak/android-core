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
package com.infomaniak.core.ksuite.myksuite.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.infomaniak.core.ksuite.myksuite.R
import com.infomaniak.core.ksuite.myksuite.ui.components.MyKSuiteChip
import com.infomaniak.core.ksuite.myksuite.ui.components.MyKSuiteTier
import com.infomaniak.core.ksuite.myksuite.ui.theme.MyKSuiteXMLTheme

abstract class BaseMyKSuiteChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val tier: MyKSuiteTier,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    @ColorInt
    private var backgroundColor: Int? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BaseMyKSuiteChipView, defStyleAttr, 0).apply {
            backgroundColor = getColor(R.styleable.BaseMyKSuiteChipView_backgroundColor, 0).takeIf {
                hasValue(R.styleable.BaseMyKSuiteChipView_backgroundColor)
            }
            recycle()
        }
    }

    @Composable
    override fun Content() {
        MyKSuiteXMLTheme { MyKSuiteChip(tier = tier, backgroundColor = backgroundColor) }
    }
}
