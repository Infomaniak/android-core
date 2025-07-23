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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType

@Composable
internal fun UrlAvatar(avatarType: AvatarType.WithInitials.Url) {
    var hasErrored by rememberSaveable { mutableStateOf(false) }

    if (hasErrored) {
        InitialsAvatar(avatarType)
    } else {
        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(avatarType.url)
            .crossfade(true)
            .build()

        AsyncImage(
            model = imageRequest,
            imageLoader = avatarType.imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { hasErrored = true },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Box(modifier = Modifier.size(32.dp)) {
                UrlAvatar(
                    AvatarType.WithInitials.Url(
                        url = "",
                        imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
                        initials = "IK",
                        colors = AvatarColors(Color(0xFF3CB572), Color.White),
                    )
                )
            }
        }
    }
}
