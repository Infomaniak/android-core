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

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.crossapplogin.front.components.MultipleAccounts
import com.infomaniak.core.crossapplogin.front.components.SelectedAccountsButton
import com.infomaniak.core.crossapplogin.front.components.SingleAccount
import com.infomaniak.core.crossapplogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crossapplogin.front.previews.AccountsPreviewParameter

val smallProgressStrokeWidth = 2.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrossLoginSelectAccounts(
    accounts: () -> List<ExternalAccount>,
    skippedIds: () -> Set<Long>,
    isLoading: () -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    customization: CrossLoginCustomization = CrossLoginDefaults.customize(),
) {

    val selectedAccounts = accounts().filter { it.id !in skippedIds() }
    val count = selectedAccounts.count()

    SelectedAccountsButton(
        customization = customization,
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(modifier = Modifier.weight(1.0f), verticalAlignment = Alignment.CenterVertically) {
            when {
                count == 1 -> {
                    SingleAccount(selectedAccounts.single(), customization, Modifier.weight(1.0f), isLoading)
                }
                count > 1 -> {
                    MultipleAccounts(selectedAccounts, customization, Modifier.weight(1.0f), isLoading)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    Surface {
        val accounts = remember { mutableStateListOf<ExternalAccount>().apply { addAll(accounts) } }
        val skippedIds = remember { mutableStateSetOf<Long>().apply { add(accounts.last().id) } }

        CrossLoginSelectAccounts(
            accounts = { accounts },
            skippedIds = { skippedIds },
            isLoading = { true },
            onClick = {},
        )
    }
}
