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

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount
import com.infomaniak.core.crossloginui.previews.AccountsPreviewParameter
import com.infomaniak.core.useravatar.AvatarData
import com.infomaniak.core.useravatar.exposed.UserAvatar

@Composable
internal fun Avatar(modifier: Modifier = Modifier, account: CrossLoginUiAccount, strokeColor: Color? = null) {
    UserAvatar(
        modifier = modifier,
        avatarData = AvatarData(uri = account.avatarUrl ?: "", userInitials = account.initials),
        border = strokeColor?.let { BorderStroke(width = 1.dp, color = it) },
    )
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<CrossLoginUiAccount>) {
    MaterialTheme {
        Surface {
            Avatar(
                account = accounts.first(),
                strokeColor = CrossLoginDefaults.customize().avatarStrokeColor,
            )
        }
    }
}
