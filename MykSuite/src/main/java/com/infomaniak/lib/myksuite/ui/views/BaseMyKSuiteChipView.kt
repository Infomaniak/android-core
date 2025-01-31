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
package com.infomaniak.lib.myksuite.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.infomaniak.lib.myksuite.R
import com.infomaniak.lib.myksuite.ui.components.MyKSuiteChip

abstract class BaseMyKSuiteChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val isMyKSuitePlus: Boolean,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    @Composable
    override fun Content() {
        MyKSuiteChip(imageRes = if (isMyKSuitePlus) R.drawable.ic_logo_my_ksuite_plus else R.drawable.ic_logo_my_ksuite)
    }
}
