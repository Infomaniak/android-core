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

import android.R
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
import com.infomaniak.core.avatar.getBackgroundColorResBasedOnId
import com.infomaniak.core.coil.ImageLoaderProvider.simpleImageLoader
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap

fun Context.getBackgroundColorBasedOnId(id: Int, @ArrayRes array: Int? = null): GradientDrawable {
    return getBackgroundColorGradientDrawable(getBackgroundColorResBasedOnId(id, array))
}

fun getBackgroundColorGradientDrawable(backgroundColorRes: Int): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(backgroundColorRes)
    }
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
    val bitmap = generateInitialsAvatarBitmap(
        size = size,
        initials = initials,
        background = background,
        initialsColor = initialsColor,
    )
    return bitmap.toDrawable(resources)
}

fun generateInitialsAvatarBitmap(
    size: Int = 350,
    initials: String,
    background: Drawable,
    @ColorInt initialsColor: Int = Color.WHITE,
): Bitmap {
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    background.setBackground(canvas)

    val paint = createInitialsAvatar(
        size = size,
        initialsColor = initialsColor,
    )

    val xPos = canvas.width / 2f
    val yPos = (canvas.height / 2f - (paint.descent() + paint.ascent()) / 2f)
    canvas.drawText(initials, xPos, yPos, paint)

    return bitmap
}

private fun createInitialsAvatar(
    size: Int = 350,
    @ColorInt initialsColor: Int = Color.WHITE,
): Paint {
    return Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = initialsColor
        textSize = (size / 2).toFloat()
    }
}
private fun Drawable.setBackground(canvas: Canvas){
    setBounds(canvas.clipBounds.left, canvas.clipBounds.top, canvas.clipBounds.right, canvas.clipBounds.bottom)
    draw(canvas)
}
