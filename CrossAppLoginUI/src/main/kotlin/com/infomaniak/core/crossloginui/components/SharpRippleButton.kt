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
package com.infomaniak.core.crossloginui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.infomaniak.core.crossloginui.theme.Dimens

@Composable
fun SharpRippleButton(
    isSelected: () -> Boolean,
    borderColor: Color?,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.buttonHeight)
            .selectable(
                selected = isSelected(),
                onClick = onClick,
            ),
        border = borderColor?.let { BorderStroke(1.dp, it) },
        shape = RoundedCornerShape(Dimens.largeCornerRadius),
        onClick = onClick,
        content = content,
        contentPadding = PaddingValues(0.dp),
    )
}
