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
package com.infomaniak.core.myksuite.ui.screens.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors

enum class ButtonType(val colors: @Composable () -> MyKSuiteButtonColors, val shape: Shape) {
    Mail(
        colors = {
            val localColors = LocalMyKSuiteColors.current
            MyKSuiteButtonColors(
                containerColor = localColors.mailButton,
                contentColor = localColors.onMailButton,
            )
        },
        shape = RoundedCornerShape(Dimens.largeCornerRadius),
    ),
    Drive(
        colors = {
            val localColors = LocalMyKSuiteColors.current
            MyKSuiteButtonColors(
                containerColor = localColors.primaryButton,
                contentColor = localColors.onPrimaryButton,
            )
        },
        shape = RoundedCornerShape(10.dp),
    ),
}

data class MyKSuiteButtonColors(val containerColor: Color, val contentColor: Color) {

    @Composable
    fun buttonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
    )
}
