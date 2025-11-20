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
@file:Suppress("NOTHING_TO_INLINE")

package com.infomaniak.core.ui.view

import android.content.Context
import android.view.View


fun Int.toPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
inline fun Int.toPx(view: View): Int = toPx(view.context)
fun Float.toPx(context: Context): Float = (this * context.resources.displayMetrics.density)
inline fun Float.toPx(view: View): Float = toPx(view.context)

fun Int.toDp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()
inline fun Int.toDp(view: View): Int = toDp(view.context)
fun Float.toDp(context: Context): Float = (this / context.resources.displayMetrics.density)
inline fun Float.toDp(view: View): Float = toDp(view.context)
