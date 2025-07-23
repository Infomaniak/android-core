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
package com.infomaniak.core.myksuite.ui.data

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.avatar.models.AvatarUrlData
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
        return AvatarType.getUrlOrInitials(
            avatarUrlData = uri?.let { AvatarUrlData(it, SingletonImageLoader.get(LocalPlatformContext.current)) },
            initials = userInitials,
            colors = AvatarColors(
                containerColor = Color(backgroundColor),
                contentColor = Color(initialsColor),
            ),
        )
    }
}
