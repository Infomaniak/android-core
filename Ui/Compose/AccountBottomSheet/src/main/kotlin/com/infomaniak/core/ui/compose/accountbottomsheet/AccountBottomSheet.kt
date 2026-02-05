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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults.ContentPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.ui.compose.basics.Dimens
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.basics.bottomsheet.ThemedBottomSheetScaffold
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ui.compose.preview.previewparameter.UserListPreviewParameterProvider
import com.infomaniak.core.common.R as RCore

// To correctly align to button horizontally, set its default content padding to 0 horizontally. Take from ButtonDefaults.TextButtonContentPadding
private val ACTION_BUTTON_CONTENT_PADDING = PaddingValues(
    start = 0.dp,
    top = ContentPadding.calculateTopPadding(),
    end = 0.dp,
    bottom = ContentPadding.calculateBottomPadding(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBottomSheet(
    onDismissRequest: () -> Unit,
    accounts: List<User>,
    selectedUserId: Int,
    onAccountClicked: (User) -> Unit,
    onAddAccount: () -> Unit,
    onLogOut: () -> Unit,
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
        Spacer(modifier = Modifier.height(Margin.Medium))
        LazyColumn(modifier = Modifier.selectableGroup()) {
            items(accounts, key = { it.id }) { user ->
                Account(
                    user = user,
                    selected = user.id == selectedUserId,
                    onAccountClick = { onAccountClicked(user) },
                    nameTextStyle = style.nameTextStyle,
                    nameColor = style.nameColor,
                    emailTextStyle = style.emailTextStyle,
                    emailColor = style.emailColor,
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = Margin.Medium, vertical = Margin.Mini))
                ActionButton(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                    text = stringResource(R.string.buttonAddAccount),
                    onClick = onAddAccount,
                    textStyle = style.actionButtonTextStyle,
                    textColor = style.actionButtonTextColor,
                )
                ActionButton(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_door_arrow_right),
                    text = stringResource(R.string.buttonLoginOut),
                    onClick = onLogOut,
                    textStyle = style.actionButtonTextStyle,
                    textColor = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
    textStyle: TextStyle,
    textColor: Color,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(onClick = onClick, shape = RectangleShape, contentPadding = ACTION_BUTTON_CONTENT_PADDING) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin.Medium),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconSize = Dimens.iconSize
            Icon(
                imageVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .padding((AVATAR_SIZE - iconSize) / 2)
                    .size(iconSize),
            )
            Text(text, style = textStyle, color = textColor)
        }
    }
}

object AccountBottomSheetDefaults {
    @Composable
    fun style(
        nameTextStyle: TextStyle = Typography.bodyMedium,
        emailTextStyle: TextStyle = Typography.bodyRegular,
        actionButtonTextStyle: TextStyle = Typography.bodyRegular,
        nameColor: Color = MaterialTheme.colorScheme.onSurface,
        emailColor: Color = MaterialTheme.colorScheme.outline,
        actionButtonTextColor: Color = MaterialTheme.colorScheme.onSurface,
    ): AccountBottomSheetStyle = AccountBottomSheetStyle(
        nameTextStyle = nameTextStyle,
        emailTextStyle = emailTextStyle,
        actionButtonTextStyle = actionButtonTextStyle,
        nameColor = nameColor,
        emailColor = emailColor,
        actionButtonTextColor = actionButtonTextColor,
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
                onAddAccount = {},
                onLogOut = {},
            )
        }
    }
}
