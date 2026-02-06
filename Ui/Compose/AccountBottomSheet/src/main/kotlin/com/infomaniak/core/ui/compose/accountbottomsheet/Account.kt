/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.ui.compose.accountbottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.basics.Dimens
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ui.compose.preview.previewparameter.UserListPreviewParameterProvider

internal val AVATAR_SIZE = 40.dp

@Composable
internal fun Account(
    user: User,
    selected: Boolean,
    onAccountClick: (User) -> Unit,
    modifier: Modifier = Modifier,
    nameTextStyle: TextStyle = AccountBottomSheetDefaults.nameTextStyle,
    nameColor: Color = AccountBottomSheetDefaults.nameColor,
    emailTextStyle: TextStyle = AccountBottomSheetDefaults.emailTextStyle,
    emailColor: Color = AccountBottomSheetDefaults.emailColor,
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = { onAccountClick(user) },
                role = Role.Button,
            )
            .padding(horizontal = Margin.Medium, vertical = Margin.Mini),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        Avatar(AvatarType.fromUser(user), modifier = Modifier.size(AVATAR_SIZE))

        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName ?: "", style = nameTextStyle, color = nameColor)
            Text(user.email, style = emailTextStyle, color = emailColor)
        }

        if (selected) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.smallIconSize),
            )
        }
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(UserListPreviewParameterProvider::class) users: List<User>) {
    MaterialTheme {
        Surface {
            Column {
                var isSelected by remember { mutableStateOf(true) }
                Account(
                    user = users.first(),
                    selected = isSelected,
                    onAccountClick = { isSelected = !isSelected },
                )
                Account(
                    user = users[1],
                    selected = false,
                    onAccountClick = {},
                )
            }
        }
    }
}
