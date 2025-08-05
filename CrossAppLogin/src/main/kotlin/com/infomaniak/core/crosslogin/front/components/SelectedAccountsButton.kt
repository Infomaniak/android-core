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
package com.infomaniak.core.crosslogin.front.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crosslogin.back.ExternalAccount
import com.infomaniak.core.crosslogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crosslogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crosslogin.front.icons.Chevron
import com.infomaniak.core.crosslogin.front.previews.AccountsPreviewParameter
import com.infomaniak.core.crosslogin.front.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectedAccountsButton(
    customization: CrossLoginCustomization,
    onClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = Margin.Medium),
    content: @Composable RowScope.() -> Unit,
) {
    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(color = customization.colors.primaryColor)
    ) {
        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight),
            border = BorderStroke(1.dp, customization.colors.buttonStrokeColor),
            shape = RoundedCornerShape(Dimens.largeCornerRadius),
            onClick = onClick,
            contentPadding = contentPadding,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Margin.Mini),
                horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                content()

                Icon(
                    imageVector = Chevron,
                    contentDescription = null,
                    tint = customization.colors.primaryColor,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    MaterialTheme {
        Surface {
            SelectedAccountsButton(
                customization = CrossLoginDefaults.customize(),
                onClick = {},
                content = { MultipleAccounts(accounts, CrossLoginDefaults.customize()) },
            )
        }
    }
}
