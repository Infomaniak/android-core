/*
 * Infomaniak Mail - Android
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
package com.infomaniak.core.ui.view.layout

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.setPadding
import com.google.android.material.R
import com.google.android.material.internal.CheckableImageButton
import com.google.android.material.textfield.TextInputLayout
import com.infomaniak.core.ui.view.extension.getAttributes
import com.infomaniak.core.ui.view.R as RView

// TODO: Waiting https://github.com/material-components/material-components-android/issues/366 (icon padding issue)
class EndIconTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textInputStyle,
) : TextInputLayout(context, attrs, defStyleAttr) {

    init {
        attrs?.getAttributes(context, RView.styleable.EndIconTextInputLayout) {
            val padding = getDimensionPixelSize(
                RView.styleable.EndIconTextInputLayout_endIconPadding,
                resources.getDimensionPixelSize(RView.dimen.marginStandardMedium),
            )
            findViewById<CheckableImageButton>(R.id.text_input_end_icon)?.setPadding(padding)
            findViewById<CheckableImageButton>(R.id.text_input_error_icon)?.setPadding(padding)
        }
    }
}
