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
package com.infomaniak.core.twofactorauth.front

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.infomaniak.core.compose.basics.Dimens
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.twofactorauth.back.ConnectionAttemptInfo
import com.infomaniak.core.twofactorauth.front.components.TwoFactorAuthAvatar

@Composable
fun AccountInfoContent(account: ConnectionAttemptInfo.TargetAccount) = Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Margin.Mini)
) {
    TwoFactorAuthAvatar(
        modifier = Modifier
            .size(Dimens.bigAvatarSize)
            .clip(CircleShape),
        account = account
    )
    Column(Modifier.weight(1f)) {
        Text(
            text = account.fullName,
            style = Typography.bodyMedium
        )
        Text(
            text = account.email,
            style = Typography.bodyRegular
        )
    }
}
