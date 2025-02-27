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
package com.infomaniak.core.myksuite.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import androidx.core.content.res.ResourcesCompat
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.MyKSuiteChip
import com.infomaniak.core.myksuite.ui.components.MyKSuiteTier
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme

abstract class BaseMyKSuiteChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val tier: MyKSuiteTier,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var backgroundColor: Int = ResourcesCompat.ID_NULL

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BaseMyKSuiteChipView, defStyleAttr, 0).apply {
            backgroundColor = getInt(R.styleable.BaseMyKSuiteChipView_backgroundColor, ResourcesCompat.ID_NULL)
            recycle()
        }
    }

    @Composable
    override fun Content() {
        val colorRes = if (backgroundColor == ResourcesCompat.ID_NULL) null else backgroundColor
        MyKSuiteTheme { MyKSuiteChip(tier = tier, backgroundColor = colorRes) }
    }
}
