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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.data.CrossLoginCustomization
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.previews.AccountsPreviewParameter
import com.infomaniak.core.crossloginui.theme.Typography
import com.infomaniak.core.login.crossapp.ExternalAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MultipleAccounts(
    accounts: List<ExternalAccount>,
    customization: CrossLoginCustomization,
    modifier: Modifier = Modifier,
) {

    val count = accounts.count()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (count == 2) {
            TwoAccountsView(accounts, customization.colors.avatarStrokeColor)
        } else {
            ThreeAccountsView(accounts, customization.colors.avatarStrokeColor)
        }

        Spacer(Modifier.width(Margin.Mini))

        Text(
            text = pluralStringResource(R.plurals.selectedAccountCountLabel, count, count),
            style = Typography.bodyMedium,
            color = customization.colors.titleColor,
        )
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    MaterialTheme {
        Surface {
            Row {
                MultipleAccounts(
                    accounts = accounts,
                    customization = CrossLoginDefaults.customize(),
                )
            }
        }
    }
}
