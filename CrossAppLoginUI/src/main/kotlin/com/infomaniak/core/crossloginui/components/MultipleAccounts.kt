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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossloginui.R
import com.infomaniak.core.crossloginui.data.CrossLoginColors
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RowScope.MultipleAccounts(
    accounts: List<CrossLoginUiAccount>,
    colors: CrossLoginColors,
) {

    val count = accounts.count()

    if (count == 2) {
        TwoAccountsView(accounts, colors)
    } else {
        ThreeAccountsView(accounts, colors)
    }

    Spacer(Modifier.width(Margin.Mini))

    Text(
        modifier = Modifier.weight(1.0f),
        text = pluralStringResource(R.plurals.selectedAccountCountLabel, count, count),
        style = Typography.bodyMedium,
        color = colors.title,
    )
}
