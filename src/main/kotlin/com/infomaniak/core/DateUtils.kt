/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val FORMAT_DATE_DEFAULT = "dd.MM.yy"
const val FORMAT_DATE_TITLE = "E d MMMM"
const val FORMAT_DATE_SIMPLE = "dd/MM/yyyy"
const val FORMAT_DATE_FULL = "EEEE d MMMM"

fun Date.format(pattern: String = FORMAT_DATE_DEFAULT): String = SimpleDateFormat(pattern, Locale.getDefault()).format(this)
