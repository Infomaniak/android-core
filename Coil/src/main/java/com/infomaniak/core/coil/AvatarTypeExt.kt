/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.avatar.models.AvatarType.DrawableResource
import com.infomaniak.core.avatar.models.AvatarType.WithInitials
import coil3.svg.SvgDecoder
import com.infomaniak.core.sentry.SentryLog



suspend fun AvatarType.toBitmap(appContext: Context): Bitmap? = when (this) {
    is WithInitials.Url -> loadAsBitmap(url = url, appContext) ?: generateWithInitials()
    is WithInitials.Initials -> generateWithInitials()
    is DrawableResource -> ContextCompat.getDrawable(appContext, resource)?.toBitmap()
}

private fun WithInitials.generateWithInitials(): Bitmap = generateInitialsAvatarBitmap(
    initials = initials,
    background = getBackgroundColorGradientDrawable(backgroundColorRes = colors.containerColor.toArgb()),
    initialsColor = colors.contentColor.toArgb(),
)

private suspend fun loadAsBitmap(url: String?, appContext: Context): Bitmap? {
    if (url.isNullOrBlank()) return null
    val size = 256
    val request = buildImageRequest(
        url = url,
        size = size,
        appContext = appContext,
    )
    return appContext.imageLoader.execute(request).toBitmap(size)
}

private fun ImageResult.toBitmap(size: Int): Bitmap? = when (this) {
    is SuccessResult -> image.toBitmap(size, size)
    is ErrorResult -> {
        SentryLog.e("AvatarTypeExt", "Failed to load avatar in Bitmap", throwable)
        null
    }
}

private fun buildImageRequest(url: String, size: Int, appContext: Context): ImageRequest {
    return ImageRequest.Builder(appContext)
        .data(url)
        .size(size)
        .networkCachePolicy(policy = CachePolicy.ENABLED)
        .allowHardware(false)
        .decoderFactory(SvgDecoder.Factory())
        .build()
}
