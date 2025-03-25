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
package com.infomaniak.core.useravatar

import android.content.res.Configuration
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.parcelize.Parcelize

@Composable
fun UserAvatarModule(avatarData: AvatarDataModule, border: BorderStroke? = null) {

    val context = LocalContext.current
    var shouldDisplayInitials by rememberSaveable(avatarData.avatarUri) { mutableStateOf(false) }
    val minAvatarSize = 32.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .sizeIn(minWidth = minAvatarSize, minHeight = minAvatarSize)
            .size(avatarData.size.dp)
            .clip(CircleShape)
            .then(if (border == null) Modifier else Modifier.border(border = border, shape = CircleShape))
            .then(if (avatarData.backgroundColorId == null) Modifier else Modifier.background(color = Color(avatarData.backgroundColorId))),
    ) {

        if (shouldDisplayInitials) {
            DefaultInitialsAvatar(avatarData)
        } else {
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
                onSuccess = { shouldDisplayInitials = false },
                onError = { shouldDisplayInitials = true },
            )
        }
    }
}

@Composable
private fun DefaultInitialsAvatar(avatarData: AvatarDataModule) = with(avatarData) {
    val isDarkMode = isSystemInDarkTheme()
    Text(
        text = userInitials,
        color = initialsColorId?.let(::Color) ?: if (isDarkMode) Color(0xFF333333) else Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.wrapContentSize(align = Alignment.Center)
    )
}

@Parcelize
data class AvatarDataModule(
    val avatarUri: String = "",
    val userInitials: String = "",
    val size: Int = 32,
    @ColorInt val initialsColorId: Int? = null,
    @ColorInt val backgroundColorId: Int? = null,
) : Parcelable

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    Surface {
        Column {
            UserAvatarModule(
                AvatarDataModule(userInitials = "IK", size = 24), border = BorderStroke(width = 1.dp, color = Color.Red)
            )
        }
    }
}
