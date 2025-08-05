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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.getBackgroundColorResBasedOnId
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.avatar.models.AvatarUrlData
import com.infomaniak.core.coil.ImageLoaderProvider
import com.infomaniak.core.crossloginui.data.CrossLoginDefaults
import com.infomaniak.core.crossloginui.previews.AccountsPreviewParameter
import com.infomaniak.core.login.crossapp.ExternalAccount

@Composable
internal fun CrossLoginAvatar(modifier: Modifier = Modifier, account: ExternalAccount, strokeColor: Color? = null) {
    val context = LocalContext.current
    val unauthenticatedImageLoader = remember(context) { ImageLoaderProvider.newImageLoader(context) }

    Avatar(
        avatarType = AvatarType.getUrlOrInitials(
            account.avatarUrl?.let { AvatarUrlData(it, unauthenticatedImageLoader) },
            initials = account.initials,
            colors = AvatarColors(
                // TODO: Adapt colors correctly for each app
                containerColor = Color(context.getBackgroundColorResBasedOnId(account.id.toInt())),
                // TODO: Adapt colors correctly for each app
                contentColor = getDefaultIconColor(),
            ),
        ),
        modifier = modifier,
        border = strokeColor?.let { BorderStroke(width = 1.dp, color = it) },
    )
}

// Copied from old UserAvatar module to keep old behavior
// TODO: Remove this when using CoreUi
private const val iconColorDark = 0xFF333333

@Composable
private fun getDefaultIconColor() = if (isSystemInDarkTheme()) Color(iconColorDark) else Color.White

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    MaterialTheme {
        Surface {
            CrossLoginAvatar(
                account = accounts.first(),
                strokeColor = CrossLoginDefaults.colors().avatarStrokeColor,
            )
        }
    }
}
