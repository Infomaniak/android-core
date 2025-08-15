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
package com.infomaniak.core.ksuite.ksuitepro.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.infomaniak.core.compose.materialthemefromxml.MaterialThemeFromXml
import com.infomaniak.core.ksuite.data.KSuite
import com.infomaniak.core.ksuite.ksuitepro.views.components.ProOfferContent

class ProOfferView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var kSuite = KSuite.ProStandard
    private var isAdmin = false
    private var onClick: (() -> Unit)? = null

    @Composable
    override fun Content() {
        MaterialThemeFromXml {
            ProOfferContent(
                kSuite = kSuite,
                isAdmin = isAdmin,
                onClick = { onClick?.invoke() },
            )
        }
    }

    fun setKSuite(kSuite: KSuite) {
        this.kSuite = kSuite
    }

    fun setIsAdmin(isAdmin: Boolean) {
        this.isAdmin = isAdmin
    }

    fun setOnClick(listener: () -> Unit) {
        onClick = listener
    }
}
