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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.previews.AccountsPreviewParameter
import com.infomaniak.core.crossloginui.theme.Dimens

@Composable
internal fun TwoAccountsView(
    accounts: List<CrossLoginUiAccount>,
    avatarStrokeColor: Color,
) {
    Box(
        modifier = Modifier.size(Dimens.avatarsBoxWidth, Dimens.avatarsBoxHeight),
        contentAlignment = Alignment.CenterStart,
    ) {

        // Right
        Avatar(
            modifier = Modifier
                .size(Dimens.avatarsBoxHeight)
                .graphicsLayer { translationX = (Dimens.avatarsBoxWidth - Dimens.avatarsBoxHeight).toPx() },
            avatar = accounts[1].avatarUrl!!,
            strokeColor = avatarStrokeColor,
        )

        // Left
        Avatar(
            modifier = Modifier.size(Dimens.avatarsBoxHeight),
            avatar = accounts[0].avatarUrl!!,
            strokeColor = avatarStrokeColor,
        )
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<CrossLoginUiAccount>) {
    MaterialTheme {
        Surface {
            TwoAccountsView(
                accounts = accounts,
                avatarStrokeColor = CrossLoginDefaults.colors().avatarStroke,
            )
        }
    }
}
