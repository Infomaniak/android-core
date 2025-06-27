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
package com.infomaniak.core.crossloginui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.infomaniak.core.useravatar.AvatarData
import com.infomaniak.core.useravatar.exposed.UserAvatar

val boxWidth = 52.dp
val boxHeight = 32.dp

@Composable
fun ThreeAvatarsView(urls: List<String>) {

    val iconSize = 24.dp

    Box(
        modifier = Modifier.size(boxWidth, boxHeight),
        contentAlignment = Alignment.CenterStart,
    ) {

        // Left
        Avatar(
            modifier = Modifier.size(iconSize),
            avatar = urls[1],
        )

        // Right
        Avatar(
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer { translationX = (boxWidth - iconSize).toPx() },
            avatar = urls[0],
        )

        // Center
        Avatar(
            modifier = Modifier
                .size(boxHeight)
                .graphicsLayer { translationX = (0.5f * (boxWidth - boxHeight)).toPx() },
            avatar = urls[2],
        )
    }
}

@Composable
fun Avatar(modifier: Modifier = Modifier, avatar: String) {
    UserAvatar(
        modifier = modifier,
        avatarData = AvatarData(uri = avatar),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.background),
    )
    // AsyncImage(
    //     modifier = modifier
    //         .clip(CircleShape)
    //         .border(1.dp, color = MaterialTheme.colorScheme.background, shape = CircleShape),
    //     contentScale = ContentScale.Crop,
    //     model = avatar,
    //     contentDescription = null,
    // )
}

@Preview
@Composable
private fun ThreeAvatarsViewPreview() {
    MaterialTheme {
        Surface {
            ThreeAvatarsView(
                urls = listOf(
                    "https://picsum.photos/id/237/200/200",
                    "https://picsum.photos/id/3/200/200",
                    "https://picsum.photos/id/10/200/200",
                )
            )
        }
    }
}
