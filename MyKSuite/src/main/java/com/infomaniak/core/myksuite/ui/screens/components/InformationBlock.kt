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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.TextWithIcon
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography

@Composable
internal fun InformationBlock(modifier: Modifier = Modifier, text: String, buttonText: String, onClick: () -> Unit) {
    val localColors = LocalMyKSuiteColors.current

    Column(
        modifier
            .background(color = localColors.informationBlockBackground, shape = RoundedCornerShape(Dimens.smallCornerRadius))
            .padding(start = Margin.Medium, top = Margin.Medium, end = Margin.Medium),
    ) {
        TextWithIcon(
            text = text,
            icon = ImageVector.vectorResource(R.drawable.ic_circle_i),
            iconTint = localColors.iconColor,
            modifier = Modifier.fillMaxWidth(),
            style = Typography.bodyRegular,
            iconRightPadding = Margin.Small,
        )
        TextButton(
            modifier = Modifier.padding(start = Margin.Medium),
            onClick = onClick,
            colors = ButtonDefaults.textButtonColors(contentColor = localColors.primaryButton),
        ) {
            Text(text = buttonText, style = Typography.bodyMedium)
        }
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {

    fun getLoremText(words: Int) = LoremIpsum(words).values.joinToString(separator = " ")

    MyKSuiteTheme {
        Surface {
            InformationBlock(text = getLoremText(35), buttonText = getLoremText(1)) {}
        }
    }
}
