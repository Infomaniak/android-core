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
package com.infomaniak.core.dotlottie.model

import android.content.Context
import androidx.annotation.ColorRes
import androidx.compose.ui.graphics.Color
import com.google.android.material.R

object LottieColor {
    fun primary0(context: Context): Color = context.colorOf(R.color.material_dynamic_primary0)
    fun primary10(context: Context): Color = context.colorOf(R.color.material_dynamic_primary10)
    fun primary20(context: Context): Color = context.colorOf(R.color.material_dynamic_primary20)
    fun primary30(context: Context): Color = context.colorOf(R.color.material_dynamic_primary30)
    fun primary40(context: Context): Color = context.colorOf(R.color.material_dynamic_primary40)
    fun primary50(context: Context): Color = context.colorOf(R.color.material_dynamic_primary50)
    fun primary60(context: Context): Color = context.colorOf(R.color.material_dynamic_primary60)
    fun primary80(context: Context): Color = context.colorOf(R.color.material_dynamic_primary80)
    fun primary90(context: Context): Color = context.colorOf(R.color.material_dynamic_primary90)
    fun primary95(context: Context): Color = context.colorOf(R.color.material_dynamic_primary95)
    fun primary99(context: Context): Color = context.colorOf(R.color.material_dynamic_primary99)
    fun primary100(context: Context): Color = context.colorOf(R.color.material_dynamic_primary100)

    private fun Context.colorOf(@ColorRes res: Int): Color = Color(getColor(res))
}
