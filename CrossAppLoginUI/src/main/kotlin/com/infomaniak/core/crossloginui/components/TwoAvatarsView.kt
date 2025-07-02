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
package com.infomaniak.core.crossloginui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.crossloginui.data.CrossLoginColors
import com.infomaniak.core.crossloginui.theme.Dimens

@Composable
fun TwoAvatarsView(urls: List<String>, colors: CrossLoginColors) {
    Box(
        modifier = Modifier.size(Dimens.avatarsBoxWidth, Dimens.avatarsBoxHeight),
        contentAlignment = Alignment.CenterStart,
    ) {

        // Right
        Avatar(
            modifier = Modifier
                .size(Dimens.avatarsBoxHeight)
                .graphicsLayer { translationX = (Dimens.avatarsBoxWidth - Dimens.avatarsBoxHeight).toPx() },
            avatar = urls[1],
            colors = colors,
        )

        // Left
        Avatar(
            modifier = Modifier.size(Dimens.avatarsBoxHeight),
            avatar = urls[0],
            colors = colors,
        )
    }
}

@Preview
@Composable
private fun TwoAvatarsViewPreview() {
    MaterialTheme {
        Surface {
            TwoAvatarsView(
                urls = listOf(
                    "https://picsum.photos/id/237/200/200",
                    "https://picsum.photos/id/3/200/200",
                ),
                colors = CrossLoginColors(Color.Gray, Color.Gray, Color.Gray, Color.Gray, Color.Gray),
            )
        }
    }
}
