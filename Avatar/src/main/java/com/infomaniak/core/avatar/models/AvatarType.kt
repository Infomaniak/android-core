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
package com.infomaniak.core.avatar.models

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.LocalAvatarColors
import com.infomaniak.core.avatar.getBackgroundColorResBasedOnId

sealed interface AvatarType {

    sealed interface WithInitials : AvatarType {
        val initials: String
        val colors: AvatarColors

        data class Initials(override val initials: String, override val colors: AvatarColors) : WithInitials {
            companion object
        }

        data class Url(
            val url: String,
            val imageLoader: ImageLoader,
            override val initials: String,
            override val colors: AvatarColors,
        ) : WithInitials {
            companion object
        }
    }

    data class DrawableResource(@DrawableRes val resource: Int) : AvatarType

    companion object {
        fun getUrlOrInitials(
            avatarUrlData: AvatarUrlData?,
            initials: String,
            colors: AvatarColors,
        ): WithInitials {
            return if (avatarUrlData == null) {
                WithInitials.Initials(initials, colors)
            } else {
                WithInitials.Url(avatarUrlData.url, avatarUrlData.imageLoader, initials, colors)
            }
        }

        @Composable
        fun fromUser(user: User): WithInitials {
            val context = LocalContext.current
            val avatarColors = LocalAvatarColors.current

            return getUrlOrInitials(
                // TODO: See if we want to customize the image loader
                avatarUrlData = user.avatar?.let { AvatarUrlData(it, SingletonImageLoader.get(context)) },
                initials = user.getInitials(),
                colors = AvatarColors(
                    // TODO: See if it works with custom dark mode local to the app
                    containerColor = getBackgroundColorResBasedOnId(user.id, avatarColors.containerColors),
                    contentColor = avatarColors.contentColor,
                ),
            )
        }
    }
}
