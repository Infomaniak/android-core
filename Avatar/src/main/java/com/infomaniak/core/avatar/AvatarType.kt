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
package com.infomaniak.core.avatar

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import coil3.ImageLoader

sealed interface AvatarType {

        sealed interface WithInitials : AvatarType {
            val initials: String
            val containerColor: Color
            val contentColor: Color

            data class Initials(
                override val initials: String,
                override val containerColor: Color,
                override val contentColor: Color,
            ) : WithInitials {
                companion object
            }

            data class Url(
                val url: String,
                val imageLoader: ImageLoader,
                override val initials: String,
                override val containerColor: Color,
                override val contentColor: Color,
            ) : WithInitials {
                companion object
            }
        }

        data class DrawableResource(@DrawableRes val resource: Int) : AvatarType

        companion object {
            fun getUrlOrInitials(
                avatarUrlData: AvatarUrlData?,
                initials: String,
                containerColor: Color,
                contentColor: Color,
            ): AvatarType {
                return if (avatarUrlData == null) {
                    WithInitials.Initials(initials, containerColor, contentColor)
                } else {
                    WithInitials.Url(avatarUrlData.url, avatarUrlData.imageLoader, initials, containerColor, contentColor)
                }
            }
        }
    }
