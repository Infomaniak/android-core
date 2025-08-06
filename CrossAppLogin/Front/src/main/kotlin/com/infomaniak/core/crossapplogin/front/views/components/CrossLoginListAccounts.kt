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
package com.infomaniak.core.crossapplogin.front.views.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.crossapplogin.front.R
import com.infomaniak.core.crossapplogin.front.components.AddAccount
import com.infomaniak.core.crossapplogin.front.components.BottomSheetItem
import com.infomaniak.core.crossapplogin.front.components.PrimaryButton
import com.infomaniak.core.crossapplogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crossapplogin.front.previews.AccountsPreviewParameter
import com.infomaniak.core.crossapplogin.front.theme.Typography
import com.infomaniak.core.R as RCore

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrossLoginListAccounts(
    accounts: SnapshotStateList<ExternalAccount>,
    skippedIds: SnapshotStateSet<Long>,
    customization: CrossLoginCustomization = CrossLoginDefaults.customize(),
    onAccountClicked: (Long) -> Unit,
    onAnotherAccountClicked: () -> Unit,
    onSaveClicked: () -> Unit,
) {

    fun ExternalAccount.isSelected(): Boolean = id !in skippedIds

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            modifier = Modifier.padding(horizontal = Margin.Medium),
            text = stringResource(R.string.selectAccountPanelTitle),
            textAlign = TextAlign.Center,
            style = Typography.bodyMedium,
            color = customization.colors.titleColor,
        )

        Spacer(Modifier.height(Margin.Medium))

        accounts.forEach { account ->
            BottomSheetItem(
                account = account,
                customization = customization,
                isSelected = { account.isSelected() },
                onClick = {
                    val selectedCount = accounts.size - skippedIds.size
                    if (account.isSelected() && selectedCount <= 1) return@BottomSheetItem
                    onAccountClicked(account.id)
                },
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(
                horizontal = Margin.Medium,
                vertical = Margin.Micro,
            ),
            color = customization.colors.buttonStrokeColor,
        )

        AddAccount(
            customization = customization,
            onClick = onAnotherAccountClicked,
        )

        Spacer(Modifier.height(Margin.Large))

        PrimaryButton(
            modifier = Modifier
                .padding(horizontal = Margin.Medium)
                .fillMaxWidth(),
            text = stringResource(RCore.string.buttonSave),
            customization = customization,
            onClick = onSaveClicked,
        )

        Spacer(Modifier.height(Margin.Medium))
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    Surface {
        CrossLoginListAccounts(
            accounts = remember { mutableStateListOf<ExternalAccount>().apply { addAll(accounts) } },
            skippedIds = remember { mutableStateSetOf<Long>().apply { add(accounts.first().id) } },
            onAccountClicked = {},
            onAnotherAccountClicked = {},
            onSaveClicked = {},
        )
    }
}
