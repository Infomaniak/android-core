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
package com.infomaniak.core.useravatar.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.infomaniak.core.useravatar.AvatarData

@Composable
internal fun InitialsTextAvatar(avatarData: AvatarData) = with(avatarData) {
    // TODO: Use BasicText with autosize when Mail and Drive will be targeting api >= 35
    Text(
        text = userInitials,
        color = iconColor?.let(::Color)
            ?: if (isSystemInDarkTheme()) Color(0xFF333333) else Color.White, // TODO use CoreUi icon color
        textAlign = TextAlign.Center,
    )
}
