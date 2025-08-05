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
package com.infomaniak.core.crosslogin.front.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crosslogin.back.ExternalAccount
import com.infomaniak.core.crosslogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crosslogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crosslogin.front.previews.AccountsPreviewParameter
import com.infomaniak.core.crosslogin.front.theme.Dimens
import com.infomaniak.core.crosslogin.front.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SingleAccount(
    account: ExternalAccount,
    customization: CrossLoginCustomization,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CrossLoginAvatar(
            modifier = Modifier
                .size(Dimens.bigAvatarSize)
                .clip(CircleShape),
            account = account,
        )

        Spacer(Modifier.width(Margin.Mini))

        Column {
            Text(
                text = account.fullName,
                style = Typography.bodyMedium,
                color = customization.colors.titleColor,
            )
            Text(
                text = account.email,
                style = Typography.bodyRegular,
                color = customization.colors.descriptionColor,
            )
        }
    }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    MaterialTheme {
        Surface {
            Row {
                SingleAccount(
                    account = accounts.first(),
                    customization = CrossLoginDefaults.customize(),
                    modifier = Modifier.weight(1.0f),
                )
            }
        }
    }
}
