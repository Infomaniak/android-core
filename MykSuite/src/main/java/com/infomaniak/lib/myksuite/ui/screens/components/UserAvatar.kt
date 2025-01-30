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
package com.infomaniak.lib.myksuite.ui.screens.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.infomaniak.lib.myksuite.R
import com.infomaniak.lib.myksuite.ui.theme.Dimens
import com.infomaniak.lib.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun UserAvatar(avatarUri: String) {
    val context = LocalContext.current
    var shouldDisplayPreview by rememberSaveable(avatarUri) { mutableStateOf(true) }

    Box {
        if (shouldDisplayPreview) {
            val imageRequest = remember(avatarUri) {
                ImageRequest.Builder(context)
                    .data(avatarUri)
                    .crossfade(true)
                    .build()
            }

            AsyncImage(
                modifier = Modifier.size(Dimens.iconSize),
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { shouldDisplayPreview = false },
            )
        } else {
            DefaultAvatar()
        }
    }
}

@Composable
private fun DefaultAvatar() {
    Icon(
        modifier = Modifier
            .size(Dimens.iconSize)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    0.0f to Color(0xFF1DDDFD),
                    0.1f to Color(0xFF337CFF),
                    0.3f to Color(0xFFA055FC),
                    0.4f to Color(0xFFF34BBB),
                    0.6f to Color(0xFFFD8C3D),
                    start = Offset(0.0f, 10.0f),
                    end = Offset(0.0f, 90.0f),
                ),
                shape = RoundedCornerShape(Dimens.largeCornerRadius),
            ),
        imageVector = ImageVector.vectorResource(R.drawable.ic_person),
        contentDescription = null,
        tint = MyKSuiteTheme.colors.iconColor,
    )
}
