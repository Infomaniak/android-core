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
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.asDrawable
import coil3.imageLoader
import coil3.load
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import coil3.svg.SvgDecoder
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.getBackgroundColorResBasedOnId
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.coil.ImageLoaderProvider.simpleImageLoader
import com.infomaniak.core.sentry.SentryLog

const val TAG = "CoilXmllExt"

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

suspend fun AvatarType.toBitmap( appContext : Context): Bitmap? = when (this) {
    is AvatarType.WithInitials.Url -> {
        loadAsBitmap(url = this.url, appContext = appContext) ?: appContext.generateInitialsAvatarDrawable(
            initials = this.initials,
            background = getBackgroundColorGradientDrawable(this.colors.containerColor.toArgb()),
            initialsColor = this.colors.contentColor.toArgb(),
        ).toBitmap()
    }
    is AvatarType.WithInitials.Initials -> {
        appContext.generateInitialsAvatarDrawable(
            initials = this.initials,
            background = getBackgroundColorGradientDrawable(this.colors.containerColor.toArgb()),
            initialsColor = this.colors.contentColor.toArgb(),
        ).toBitmap()
    }
    is AvatarType.DrawableResource -> {
        ContextCompat.getDrawable(appContext, this.resource)?.toBitmap()
    }
    else -> null
}

private suspend fun loadAsBitmap(url: String?, size: Int = 256, appContext: Context): Bitmap? {
    if (url.isNullOrBlank()) return null

    val imageLoader = appContext.imageLoader

    val request = ImageRequest.Builder(appContext)
        .data(url)
        .size(size)
        .networkCachePolicy(policy = CachePolicy.ENABLED)
        .allowHardware(false)
        .decoderFactory(SvgDecoder.Factory())
        .build()


    return when (val result = imageLoader.execute(request)) {
        is SuccessResult -> result.image.asDrawable(appContext.resources).toBitmap()
        is ErrorResult -> {
            SentryLog.e(TAG, "Failed to load SVG avatar", result.throwable)
            null
        }
    }
}
