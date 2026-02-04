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
package com.infomaniak.core.ui.compose.accountbottomsheet

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.basics.bottomsheet.ThemedBottomSheetScaffold
import com.infomaniak.core.ui.compose.preview.previewparameter.UserListPreviewParameterProvider
import com.infomaniak.core.common.R as RCore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBottomSheet(
    onDismissRequest: () -> Unit,
    accounts: List<User>,
    selectedUserId: Int,
    onAccountClicked: (User) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    style: AccountBottomSheetStyle = AccountBottomSheetDefaults.style(),
) {
    ThemedBottomSheetScaffold(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        title = pluralStringResource(RCore.plurals.myAccount, accounts.count()),
    ) {
        LazyColumn {
            items(accounts, key = { it.id }) { user ->
                Account(
                    user = user,
                    selected = user.id == selectedUserId,
                    onAccountClick = { onAccountClicked(user) },
                    style = style,
                )
            }
        }
    }
}

object AccountBottomSheetDefaults {
    @Composable
    fun style(
        nameTextStyle: TextStyle = Typography.bodyMedium,
        emailTextStyle: TextStyle = Typography.bodyRegular,
        nameColor: Color = MaterialTheme.colorScheme.onBackground,
        emailColor: Color = MaterialTheme.colorScheme.outline,
    ): AccountBottomSheetStyle = AccountBottomSheetStyle(
        nameTextStyle = nameTextStyle,
        emailTextStyle = emailTextStyle,
        nameColor = nameColor,
        emailColor = emailColor,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Preview(@PreviewParameter(UserListPreviewParameterProvider::class) users: List<User>) {
    MaterialTheme {
        Surface {
            AccountBottomSheet(
                onDismissRequest = {}, accounts = users,
                selectedUserId = users.first().id,
                onAccountClicked = {},
            )
        }
    }
}
