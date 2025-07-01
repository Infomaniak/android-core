/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
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
package com.infomaniak.core.crossloginui.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.components.AccountItem
import com.infomaniak.core.crossloginui.components.PrimaryButton
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.icons.AddUser
import com.infomaniak.core.crossloginui.theme.Dimens
import com.infomaniak.core.crossloginui.theme.Typography
import com.infomaniak.core.R as RCore

@Composable
fun CrossLoginListAccounts(
    accounts: () -> SnapshotStateList<CrossLoginUiAccount>,
    onAccountClicked: (CrossLoginUiAccount) -> Unit,
    onAnotherAccountClicked: () -> Unit,
    onCloseClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            modifier = Modifier.padding(horizontal = Margin.Medium),
            text = stringResource(R.string.selectAccountPanelTitle),
            textAlign = TextAlign.Center,
            style = Typography.bodyMedium,
            // color = localColors.primaryTextColor, // TODO
        )
        Spacer(Modifier.height(Margin.Medium))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
            accounts().forEach { account ->
                AccountItem(
                    title = account.name,
                    description = account.email,
                    iconUrl = account.avatarUrl,
                    isSelected = { account.isSelected },
                    onClick = {
                        if (account.isSelected && accounts().count { it.isSelected } <= 1) return@AccountItem
                        onAccountClicked(account)
                    },
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(
                horizontal = Margin.Medium,
                vertical = Margin.Micro,
            ),
        )
        AccountItem(
            title = stringResource(R.string.buttonUseOtherAccount),
            icon = AddUser,
            onClick = { onAnotherAccountClicked() },
        )
        Spacer(Modifier.height(Margin.Large))
        PrimaryButton(
            modifier = Modifier
                .padding(horizontal = Margin.Medium)
                .fillMaxWidth(),
            text = stringResource(RCore.string.buttonSave),
            shape = RoundedCornerShape(Dimens.largeCornerRadius),
            onClick = onCloseClicked,
        )
        Spacer(Modifier.height(Margin.Medium))
    }
}

@Preview
@Composable
private fun Preview() {
    Surface {
        CrossLoginListAccounts(
            accounts = { mutableStateListOf() },
            onAccountClicked = {},
            onAnotherAccountClicked = {},
            onCloseClicked = {},
        )
    }
}
