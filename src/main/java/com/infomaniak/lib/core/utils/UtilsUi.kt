/*
 * Infomaniak Core - Android
 * Copyright (C) 2021 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform

import com.infomaniak.lib.core.R
import java.util.*

object UtilsUi {

    fun Context.openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
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

    fun String.getInitials(): String {
        this.split(" ").let { initials ->
            val initialFirst = initials.firstOrNull()?.firstOrNull()?.toUpperCase() ?: ""
            val initialSecond = initials.getOrNull(1)?.firstOrNull()?.toUpperCase() ?: ""
            return@getInitials "$initialFirst$initialSecond"
        }
    }

    fun Context.getBackgroundColorBasedOnId(id: Int): GradientDrawable {
        val colorIndex = id % 9
        val organizationColor = this.resources.getIntArray(R.array.organizationColors)[colorIndex]
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(organizationColor)
        }
    }
}