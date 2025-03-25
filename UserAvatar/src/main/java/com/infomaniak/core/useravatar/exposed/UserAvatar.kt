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
package com.infomaniak.core.useravatar.exposed

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.infomaniak.core.useravatar.AvatarData
import com.infomaniak.core.useravatar.AvatarDisplayState
import com.infomaniak.core.useravatar.component.DefaultIconAvatar
import com.infomaniak.core.useravatar.component.InitialsTextAvatar

@Composable
fun UserAvatar(modifier: Modifier = Modifier, avatarData: AvatarData, border: BorderStroke? = null) {

    val context = LocalContext.current
    var isAvatarError by rememberSaveable(avatarData.avatarUri) { mutableStateOf(false) }
    var avatarDisplayState by rememberSaveable(avatarData, isAvatarError) {
        val avatarState = when {
            avatarData.avatarUri.isNotBlank() -> AvatarDisplayState.Avatar
            avatarData.userInitials.isNotBlank() -> AvatarDisplayState.Initials
            else -> AvatarDisplayState.UnknownUser
        }
        mutableStateOf(avatarState)
    }

    val minAvatarSize = 32.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = minAvatarSize, minHeight = minAvatarSize)
            .clip(CircleShape)
            .then(if (border == null) Modifier else Modifier.border(border = border, shape = CircleShape))
            .then(if (avatarData.backgroundColorId == null) Modifier else Modifier.background(color = Color(avatarData.backgroundColorId))),
    ) {
        when {
            isAvatarError && avatarDisplayState == AvatarDisplayState.Initials -> InitialsTextAvatar(avatarData)
            isAvatarError -> DefaultIconAvatar(avatarData.iconColorId)
            else -> {
                val imageRequest = remember(avatarData.avatarUri, context) {
                    ImageRequest.Builder(context)
                        .data(avatarData.avatarUri)
                        .crossfade(true)
                        .build()
                }

                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = { isAvatarError = false },
                    onError = { isAvatarError = true },
                )
            }
        }
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    Surface {
        Column {
            UserAvatar(
                avatarData = AvatarData(),
                border = BorderStroke(width = 1.dp, color = Color.Red),
            )
            UserAvatar(
                avatarData = AvatarData(userInitials = "IK"),
                border = BorderStroke(width = 1.dp, color = Color.Red),
            )
        }
    }
}
