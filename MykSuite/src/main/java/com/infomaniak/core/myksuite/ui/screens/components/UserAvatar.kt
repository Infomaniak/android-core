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
package com.infomaniak.core.myksuite.ui.screens.components

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.myKSuiteGradient
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun UserAvatar(avatarUri: String) {

    val context = LocalContext.current
    var shouldDisplayPreview by rememberSaveable(avatarUri) { mutableStateOf(true) }

    Box(
        Modifier
            .border(border = myKSuiteGradient(), shape = CircleShape)
            .clip(CircleShape)
    ) {
        if (shouldDisplayPreview) {
            val imageRequest = remember(avatarUri, context) {
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
        modifier = Modifier.size(Dimens.iconSize),
        imageVector = ImageVector.vectorResource(R.drawable.ic_person),
        contentDescription = null,
        tint = LocalMyKSuiteColors.current.iconColor,
    )
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            UserAvatar("")
        }
    }
}
