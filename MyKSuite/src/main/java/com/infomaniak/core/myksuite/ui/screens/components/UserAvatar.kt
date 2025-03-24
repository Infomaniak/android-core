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
package com.infomaniak.core.myksuite.ui.screens.components

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import coil3.request.*
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.myKSuiteGradient
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import kotlinx.parcelize.Parcelize

@Composable
internal fun UserAvatar(avatarData: AvatarData) {

    val context = LocalContext.current
    var shouldDisplayLoadingIcon by rememberSaveable(avatarData.avatarUri) { mutableStateOf(false) }

    val initialsAvatar = context.generateInitialsAvatarDrawable(
        initials = avatarData.userInitials,
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(avatarData.backgroundColorId)
        }
    )
    Box(
        Modifier
            .border(border = myKSuiteGradient(), shape = CircleShape)
            .clip(CircleShape)
    ) {
        val imageRequest = remember(avatarData.avatarUri, context) {
            ImageRequest.Builder(context)
                .data(avatarData.avatarUri)
                .placeholder(initialsAvatar)
                .fallback(initialsAvatar)
                .error(initialsAvatar)
                .crossfade(true)
                .build()
        }

        AsyncImage(
            modifier = Modifier.size(Dimens.iconSize),
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onLoading = {
            },
            onSuccess = { shouldDisplayLoadingIcon = false },
            onError = { shouldDisplayLoadingIcon = false },
        )
    }
}

@Composable
private fun DefaultAvatar() {
    Icon(
        modifier = Modifier.size(Dimens.iconSize),
        imageVector = ImageVector.vectorResource(R.drawable.ic_person),
        contentDescription = null,
        tint = LocalMyKSuiteColors.current.iconColor,
    )
}


fun Context.generateInitialsAvatarDrawable(
    size: Int = 350,
    initials: String,
    background: Drawable,
    @ColorInt initialsColor: Int = Color.WHITE,
): Drawable {
    val bitmap = createBitmap(size, size)
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
    return bitmap.toDrawable(resources)
}

@Parcelize
data class AvatarData(
    val avatarUri: String = "",
    val userInitials: String = "",
    val userId: Int = -1,
    @ColorRes val backgroundColorId: Int = ResourcesCompat.ID_NULL,
) : Parcelable

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            UserAvatar(AvatarData(userInitials = "IK"))
        }
    }
}
