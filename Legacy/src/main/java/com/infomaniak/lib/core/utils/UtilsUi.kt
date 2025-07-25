/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2025 Infomaniak Network SA
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
package com.infomaniak.lib.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.infomaniak.lib.core.R

object UtilsUi {

    fun Context.openUrl(url: String) {
        try {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.also(::startActivity)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            showToast(R.string.browserNotFound)
        }
    }

    fun setupSharedElementTransition(fragment: Fragment, @IdRes startViewId: Int, @IdRes endViewId: Int) {
        val enterContainerTransform = MaterialContainerTransform()
        enterContainerTransform.startViewId = startViewId
        enterContainerTransform.endViewId = endViewId
        enterContainerTransform.duration = 200
        enterContainerTransform.interpolator = FastOutSlowInInterpolator()
        enterContainerTransform.setPathMotion(MaterialArcMotion())
        enterContainerTransform.setAllContainerColors(ContextCompat.getColor(fragment.requireContext(), R.color.background))
        fragment.sharedElementEnterTransition = enterContainerTransform

        val returnContainerTransform = MaterialContainerTransform()
        returnContainerTransform.startViewId = endViewId
        returnContainerTransform.endViewId = startViewId
        returnContainerTransform.duration = 175
        returnContainerTransform.setPathMotion(MaterialArcMotion())
        returnContainerTransform.interpolator = FastOutSlowInInterpolator()
        returnContainerTransform.setAllContainerColors(ContextCompat.getColor(fragment.requireContext(), R.color.background))
        fragment.sharedElementReturnTransition = returnContainerTransform
    }

    @Deprecated("Use the method exposed through the Core:Avatar module")
    fun Context.getBackgroundColorBasedOnId(id: Int, @ArrayRes array: Int? = null): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(getBackgroundColorResBasedOnId(id, array))
        }
    }

    @Deprecated(
        message = "This has been extracted to Core:Avatar module",
        replaceWith = ReplaceWith(
            expression = "getBackgroundColorResBasedOnId(id, array)",
            imports = ["com.infomaniak.core.coil.getBackgroundColorResBasedOnId"],
        ),
    )
    fun Context.getBackgroundColorResBasedOnId(id: Int, @ArrayRes array: Int? = null): Int {
        val arrayResource = array ?: R.array.organizationColors
        val colors = resources.getIntArray(arrayResource)
        val colorIndex = Math.floorMod(id, colors.count())
        val organizationColor = colors[colorIndex]

        return organizationColor
    }

    @Deprecated("Use the method exposed through the Core:Avatar module")
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
}
