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
package com.infomaniak.core.myksuite.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.screens.components.ButtonType
import com.infomaniak.core.myksuite.ui.screens.components.MyKSuiteButtonColors
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.Margin
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography

@Composable
internal fun MyKSuitePrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    colors: @Composable () -> MyKSuiteButtonColors,
    shape: Shape,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.height(Dimens.buttonHeight),
        colors = colors().buttonColors(),
        shape = shape,
        onClick = onClick,
    ) {
        Text(text = text, style = Typography.bodyMedium)
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(Margin.Large)) {
                MyKSuitePrimaryButton(
                    text = "Lorem",
                    shape = ButtonType.Mail.shape,
                    colors = ButtonType.Mail.colors,
                    onClick = {},
                )
                MyKSuitePrimaryButton(
                    text = "Close",
                    shape = ButtonType.Drive.shape,
                    colors = ButtonType.Drive.colors,
                    onClick = {},
                )
            }
        }
    }
}
