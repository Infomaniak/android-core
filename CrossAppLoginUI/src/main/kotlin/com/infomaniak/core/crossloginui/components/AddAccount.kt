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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.data.CrossLoginColors
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.icons.AddUser
import com.infomaniak.core.crossloginui.theme.Dimens
import com.infomaniak.core.crossloginui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddAccount(
    colors: CrossLoginColors,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(color = colors.primary)
    ) {
        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight),
            shape = RectangleShape,
            onClick = onClick,
            contentPadding = PaddingValues(0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin.Medium, vertical = Margin.Mini),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Box(Modifier.padding(all = Margin.Mini)) {
                    Icon(
                        imageVector = AddUser,
                        modifier = Modifier.size(Dimens.iconSize),
                        contentDescription = null,
                        tint = colors.primary,
                    )
                }

                Spacer(Modifier.width(Margin.Mini))

                Text(
                    text = stringResource(R.string.buttonUseOtherAccount),
                    style = Typography.bodyRegular,
                    color = colors.title,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            AddAccount(
                colors = CrossLoginDefaults.colors(),
                onClick = {},
            )
        }
    }
}
