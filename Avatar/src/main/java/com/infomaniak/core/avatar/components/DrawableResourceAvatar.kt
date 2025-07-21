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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.avatar.AvatarType
import com.infomaniak.core.avatar.R

@Composable
internal fun DrawableResourceAvatar(avatarType: AvatarType.DrawableResource) {
    Image(
        modifier = Modifier.fillMaxSize(),
        imageVector = ImageVector.vectorResource(avatarType.resource),
        contentDescription = null,
    )
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Box(modifier = Modifier.size(32.dp)) {
                DrawableResourceAvatar(AvatarType.DrawableResource(R.drawable.ic_exemple_drawable_res))
            }
        }
    }
}
