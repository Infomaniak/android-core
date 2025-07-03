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
import com.infomaniak.core.crossloginui.components.AddAccount
import com.infomaniak.core.crossloginui.components.BottomSheetItem
import com.infomaniak.core.crossloginui.components.PrimaryButton
import com.infomaniak.core.crossloginui.data.CrossLoginColors
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.theme.Dimens
import com.infomaniak.core.crossloginui.theme.Typography
import com.infomaniak.core.R as RCore

@Composable
internal fun CrossLoginListAccounts(
    accounts: () -> SnapshotStateList<CrossLoginUiAccount>,
    colors: CrossLoginColors,
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
            color = colors.title,
        )
        Spacer(Modifier.height(Margin.Medium))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
            accounts().forEach { account ->
                BottomSheetItem(
                    account = account,
                    colors = colors,
                    isSelected = { account.isSelected },
                    onClick = {
                        if (account.isSelected && accounts().count { it.isSelected } <= 1) return@BottomSheetItem
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
        AddAccount(
            colors = colors,
            onClick = { onAnotherAccountClicked() },
        )
        Spacer(Modifier.height(Margin.Large))
        PrimaryButton(
            modifier = Modifier
                .padding(horizontal = Margin.Medium)
                .fillMaxWidth(),
            text = stringResource(RCore.string.buttonSave),
            shape = RoundedCornerShape(Dimens.largeCornerRadius),
            colors = colors,
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
            colors = CrossLoginDefaults.colors(),
            onAccountClicked = {},
            onAnotherAccountClicked = {},
            onCloseClicked = {},
        )
    }
}
