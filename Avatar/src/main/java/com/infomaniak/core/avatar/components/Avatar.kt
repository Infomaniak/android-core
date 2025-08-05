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
package com.infomaniak.core.avatar.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.infomaniak.core.avatar.R
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType

@Composable
fun Avatar(
    avatarType: AvatarType,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    shape: Shape = CircleShape,
) {
    Box(
        modifier
            .size(32.dp)
            .clip(shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        when (avatarType) {
            is AvatarType.WithInitials.Initials -> InitialsAvatar(avatarType)
            is AvatarType.WithInitials.Url -> UrlAvatar(avatarType)
            is AvatarType.DrawableResource -> DrawableResourceAvatar(avatarType)
        }
    }
}

@Preview
@Composable
private fun AvatarPreview() {
    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Avatar(
                    AvatarType.WithInitials.Initials("IK", AvatarColors(Color(0xFF3CB572), Color.White)),
                    modifier = Modifier.size(32.dp),
                )
                Avatar(
                    AvatarType.WithInitials.Url(
                        url = "",
                        imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
                        initials = "IK",
                        colors = AvatarColors(Color(0xFF3CB572), Color.White),
                    ),
                    modifier = Modifier.size(48.dp),
                )
                Avatar(
                    AvatarType.DrawableResource(R.drawable.ic_exemple_drawable_res),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
