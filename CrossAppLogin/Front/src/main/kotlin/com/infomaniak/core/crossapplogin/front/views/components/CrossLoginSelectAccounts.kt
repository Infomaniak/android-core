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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.core.crossapplogin.front.components.MultipleAccounts
import com.infomaniak.core.crossapplogin.front.components.SelectedAccountsButton
import com.infomaniak.core.crossapplogin.front.components.SingleAccount
import com.infomaniak.core.crossapplogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crossapplogin.front.previews.AccountsPreviewParameter
import com.infomaniak.core.crossapplogin.back.ExternalAccount

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrossLoginSelectAccounts(
    accounts: SnapshotStateList<ExternalAccount>,
    skippedIds: SnapshotStateSet<Long>,
    customization: CrossLoginCustomization = CrossLoginDefaults.customize(),
    onClick: () -> Unit,
) {

    val selectedAccounts = accounts.filter { it.id !in skippedIds }
    val count = selectedAccounts.count()

    SelectedAccountsButton(
        customization = customization,
        onClick = onClick,
    ) {
        when {
            count == 1 -> SingleAccount(selectedAccounts.single(), customization, Modifier.weight(1.0f))
            count > 1 -> MultipleAccounts(selectedAccounts, customization, Modifier.weight(1.0f))
        }
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    Surface {
        CrossLoginSelectAccounts(
            accounts = remember { mutableStateListOf<ExternalAccount>().apply { addAll(accounts) } },
            skippedIds = remember { mutableStateSetOf<Long>().apply { add(accounts.last().id) } },
            onClick = {},
        )
    }
}
