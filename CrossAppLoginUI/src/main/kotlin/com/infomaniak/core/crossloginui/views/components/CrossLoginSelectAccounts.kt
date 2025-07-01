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

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.compose.materialthemefromxml.MaterialThemeFromXml
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.components.CrossLoginItem
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrossLoginSelectAccounts(
    accounts: () -> SnapshotStateList<CrossLoginUiAccount>,
    onClick: () -> Unit,
) {
    MaterialThemeFromXml {
        val selectedAccounts = accounts().filter { it.isSelected }
        val count = selectedAccounts.count()
        when {
            count == 1 -> {
                val account = selectedAccounts.first()
                CrossLoginItem(
                    title = account.name,
                    description = account.email,
                    iconUrl = account.avatarUrl,
                    isSelected = { true },
                    onClick = onClick,
                )
            }
            count > 1 -> {
                CrossLoginItem(
                    title = pluralStringResource(R.plurals.selectedAccountCountLabel, count, count),
                    iconsUrls = selectedAccounts.mapNotNull { it.avatarUrl },
                    isSelected = { true },
                    onClick = onClick,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Surface {
        CrossLoginSelectAccounts(
            accounts = { mutableStateListOf() },
            onClick = {},
        )
    }
}
