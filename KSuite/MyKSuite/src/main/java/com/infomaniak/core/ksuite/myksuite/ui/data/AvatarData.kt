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
package com.infomaniak.core.ksuite.myksuite.ui.data

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.avatar.models.AvatarUrlData
import com.infomaniak.core.coil.ImageLoaderProvider
import kotlinx.parcelize.Parcelize

@Parcelize
data class AvatarData(
    val id: String, // Used for `backgroundColor` computation (ie. the `user.id`, or `correspondent.email`)
    val uri: String?,
    val userInitials: String,
    @ColorInt val initialsColor: Int,
    @ColorInt val backgroundColor: Int,
) : Parcelable {
    @Composable
    fun toAvatarType(): AvatarType {
        val context = LocalContext.current
        val unauthenticatedImageLoader = remember(context) { ImageLoaderProvider.newImageLoader(context) }

        return AvatarType.getUrlOrInitials(
            avatarUrlData = uri?.let { AvatarUrlData(it, unauthenticatedImageLoader) },
            initials = userInitials,
            colors = AvatarColors(
                containerColor = Color(backgroundColor),
                contentColor = Color(initialsColor),
            ),
        )
    }
}
