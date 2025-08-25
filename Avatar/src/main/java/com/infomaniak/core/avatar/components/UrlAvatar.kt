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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType

@Composable
internal fun UrlAvatar(avatarType: AvatarType.WithInitials.Url) {
    // Show avatars in preview mode because we can't resolve api calls. The coil3 state will always be in the Empty state which
    // displays nothing otherwise. Can be removed if we support custom LocalAsyncImagePreviewHandler in preview mode for images.
    if (LocalInspectionMode.current) InitialsAvatar(avatarType)

    var state by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    Crossfade(state) { localState ->
        when (localState) {
            is AsyncImagePainter.State.Error,
            is AsyncImagePainter.State.Loading -> InitialsAvatar(avatarType)
            // Don't show anything on empty state because when everything is loaded, the first frame will always have the Empty
            // state which would otherwise display the initials for a single frame when the image has already been loaded anyway.
            // I.e. makes it easier on the eye when connection speed is high.
            AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Success -> Unit
        }
    }

    if (state !is AsyncImagePainter.State.Error) {
        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(avatarType.url)
            .crossfade(true)
            .build()

        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = imageRequest,
            imageLoader = avatarType.imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onState = { state = it },
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
