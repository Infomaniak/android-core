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
package com.infomaniak.core.coil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import coil3.ImageLoader
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.coil.CoilXmlUtils.simpleImageLoader

fun Context.getBackgroundColorBasedOnId(id: Int, @ArrayRes array: Int? = null): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(getBackgroundColorResBasedOnId(id, array))
    }
}

// TODO: Move this to generic avatar module when it's created
fun Context.getBackgroundColorResBasedOnId(id: Int, @ArrayRes array: Int? = null): Int {
    val arrayResource = array ?: R.array.organizationColors
    val colors = resources.getIntArray(arrayResource)
    val colorIndex = Math.floorMod(id, colors.count())
    val organizationColor = colors[colorIndex]

    return organizationColor
}

fun ImageView.loadAvatar(
    user: User,
    imageLoader: ImageLoader = context.simpleImageLoader,
) = loadAvatar(user.id, user.avatar, user.getInitials(), imageLoader)

fun ImageView.loadAvatar(
    id: Int,
    avatarUrl: String?,
    initials: String,
    imageLoader: ImageLoader = context.simpleImageLoader,
    @ColorInt initialsColor: Int = Color.WHITE,
) {
    val backgroundColor = context.getBackgroundColorBasedOnId(id)
    loadAvatar(backgroundColor, avatarUrl, initials, imageLoader, initialsColor)
}

fun ImageView.loadAvatar(
    backgroundColor: GradientDrawable,
    avatarUrl: String?,
    initials: String,
    imageLoader: ImageLoader = context.simpleImageLoader,
    @ColorInt initialsColor: Int = Color.WHITE,
) {
    val fallback = context.generateInitialsAvatarDrawable(
        initials = initials,
        background = backgroundColor,
        initialsColor = initialsColor,
    )
    load(avatarUrl, imageLoader) {
        error(fallback)
        fallback(fallback)
        placeholder(fallback)
    }
}

fun Context.generateInitialsAvatarDrawable(
    size: Int = 350,
    initials: String,
    background: Drawable,
    @ColorInt initialsColor: Int = Color.WHITE,
): Drawable {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    background.setBounds(canvas.clipBounds.left, canvas.clipBounds.top, canvas.clipBounds.right, canvas.clipBounds.bottom)
    background.draw(canvas)
    Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = initialsColor
        textSize = (size / 2).toFloat()

        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (descent() + ascent()) / 2)
        canvas.drawText(initials, xPos.toFloat(), yPos, this)
    }
    return BitmapDrawable(this.resources, bitmap)
}
