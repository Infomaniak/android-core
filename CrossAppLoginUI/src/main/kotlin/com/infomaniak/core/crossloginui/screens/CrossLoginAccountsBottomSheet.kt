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
package com.infomaniak.core.crossloginui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.icons.AddUser
import com.infomaniak.core.crossloginui.screens.components.CrossLoginItem
import com.infomaniak.core.crossloginui.screens.components.PrimaryButton
import com.infomaniak.core.crossloginui.theme.Dimens
import com.infomaniak.core.crossloginui.theme.LocalCrossLoginColors
import com.infomaniak.core.crossloginui.theme.Typography
import kotlinx.coroutines.launch
import com.infomaniak.core.R as RCore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossLoginAccountsBottomSheet(
    modifier: Modifier = Modifier,
    accounts: () -> List<CrossLoginUiAccount>,
    onAccountClicked: (CrossLoginUiAccount) -> Unit,
    onAnotherAccountClicked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest, modifier, sheetState) {
        val scope = rememberCoroutineScope()
        CrossLoginAccountsBottomSheetContent(
            accounts = accounts,
            onAccountClicked = onAccountClicked,
            onAnotherAccountClicked = onAnotherAccountClicked,
            onCloseClicked = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    onDismissRequest()
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CrossLoginAccountsBottomSheetContent(
    accounts: () -> List<CrossLoginUiAccount>,
    onAccountClicked: (CrossLoginUiAccount) -> Unit,
    onAnotherAccountClicked: () -> Unit,
    onCloseClicked: () -> Unit,
) {
    val localColors = LocalCrossLoginColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val paddedModifier = Modifier.padding(horizontal = Margin.Large)
        Text(
            modifier = paddedModifier,
            text = stringResource(R.string.selectAccountPanelTitle),
            textAlign = TextAlign.Center,
            style = Typography.h2,
            color = localColors.primaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Margin.Mini)) {
            accounts().forEach { account ->
                CrossLoginItem(
                    title = account.name,
                    description = account.email,
                    iconUrl = account.avatarUrl,
                    isSelected = { account.isSelected },
                    onClick = {
                        if (account.isSelected && accounts().count { it.isSelected } <= 1) return@CrossLoginItem
                        onAccountClicked(account)
                    },
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(
                horizontal = Margin.Medium,
                vertical = Margin.Small,
            ),
        )
        CrossLoginItem(
            title = stringResource(R.string.buttonUseOtherAccount),
            icon = AddUser,
            onClick = {
                onCloseClicked()
                onAnotherAccountClicked()
            },
        )
        Spacer(Modifier.height(Margin.Huge))
        PrimaryButton(
            modifier = paddedModifier.fillMaxWidth(),
            text = stringResource(RCore.string.buttonSave),
            shape = RoundedCornerShape(Dimens.largeCornerRadius),
            onClick = onCloseClicked,
        )
        Spacer(Modifier.height(Margin.Large))
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    Surface {
        CrossLoginAccountsBottomSheetContent(
            accounts = { emptyList() },
            onAccountClicked = {},
            onAnotherAccountClicked = {},
            onCloseClicked = {},
        )
    }
}
