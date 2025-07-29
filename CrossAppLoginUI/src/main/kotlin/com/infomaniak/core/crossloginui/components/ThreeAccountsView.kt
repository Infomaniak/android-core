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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.previews.AccountsPreviewParameter
import com.infomaniak.core.crossloginui.theme.Dimens
import com.infomaniak.core.login.crossapp.ExternalAccount

@Composable
internal fun ThreeAccountsView(
    accounts: List<ExternalAccount>,
    avatarStrokeColor: Color,
) {
    Box(contentAlignment = Alignment.Center) {

        Row {
            // Left
            Avatar(
                modifier = Modifier.size(Dimens.iconSize),
                account = accounts[1],
                strokeColor = avatarStrokeColor,
            )

            Spacer(Modifier.width(width = Dimens.avatarsBoxWidth - (Dimens.iconSize + Dimens.iconSize)))

            // Right
            Avatar(
                modifier = Modifier.size(Dimens.iconSize),
                account = accounts[2],
                strokeColor = avatarStrokeColor,
            )
        }

        // Center
        Avatar(
            modifier = Modifier.size(Dimens.avatarsBoxHeight),
            account = accounts[0],
            strokeColor = avatarStrokeColor,
        )
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    MaterialTheme {
        Surface {
            ThreeAccountsView(
                accounts = accounts,
                avatarStrokeColor = CrossLoginDefaults.colors().avatarStrokeColor,
            )
        }
    }
}
