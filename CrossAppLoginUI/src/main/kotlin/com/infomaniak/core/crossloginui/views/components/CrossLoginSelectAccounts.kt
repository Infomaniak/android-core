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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.pluralStringResource
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.components.AccountItem
import com.infomaniak.core.crossloginui.data.CrossLoginColors
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.icons.Chevron

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrossLoginSelectAccounts(
    accounts: () -> SnapshotStateList<CrossLoginUiAccount>,
    colors: CrossLoginColors,
    onClick: () -> Unit,
) {
    val selectedAccounts = accounts().filter { it.isSelected }
    val count = selectedAccounts.count()
    when {
        count == 1 -> {
            val account = selectedAccounts.first()
            AccountItem(
                title = account.name,
                description = account.email,
                iconUrl = account.avatarUrl,
                endIcon = Chevron,
                hasBorder = true,
                colors = colors,
                isSelected = { true },
                onClick = onClick,
            )
        }
        count > 1 -> {
            AccountItem(
                title = pluralStringResource(R.plurals.selectedAccountCountLabel, count, count),
                iconsUrls = selectedAccounts.mapNotNull { it.avatarUrl },
                endIcon = Chevron,
                hasBorder = true,
                colors = colors,
                isSelected = { true },
                onClick = onClick,
            )
        }
    }
}
