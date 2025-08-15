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
package com.infomaniak.core.crossapplogin.front.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossapplogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults

@Composable
internal fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    customization: CrossLoginCustomization,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.height(customization.buttonStyle.height),
        colors = ButtonDefaults.buttonColors(
            containerColor = customization.colors.primaryColor,
            contentColor = customization.colors.onPrimaryColor,
        ),
        shape = customization.buttonStyle.shape,
        onClick = onClick,
    ) {
        Text(text = text, style = Typography.bodyMedium)
    }
}

@Preview
@Composable
private fun Preview() {
    Surface {
        Column(verticalArrangement = Arrangement.spacedBy(Margin.Large)) {
            PrimaryButton(
                text = "Lorem",
                customization = CrossLoginDefaults.customize(),
                onClick = {},
            )
        }
    }
}
