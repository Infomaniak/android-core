/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
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

import java.text.SimpleDateFormat
import java.util.*

const val FORMAT_DATE_CLEAR_MONTH = "dd MMM yyyy"
const val FORMAT_DATE_CLEAR_MONTH_DAY_ONE_CHAR = "d MMM yyyy"
const val FORMAT_DATE_DEFAULT = "dd.MM.yy"
const val FORMAT_DATE_HOUR_MINUTE = "HH:mm"
const val FORMAT_DATE_SHORT_DAY_ONE_CHAR = "d MMM"
const val FORMAT_DATE_WITH_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ssZ"
const val FORMAT_EVENT_DATE = "dd/MM/yyyy HH:mm"
const val FORMAT_FULL_DATE = "EEEE dd MMMM yyyy"
const val FORMAT_NEW_FILE = "yyyyMMdd_HHmmss"

fun Date.format(pattern: String = FORMAT_DATE_DEFAULT): String {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}

fun Date.startOfTheDay(): Date =
    Calendar.getInstance().apply {
        time = this@startOfTheDay
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.time

fun Date.endOfTheDay(): Date =
    Calendar.getInstance().apply {
        time = this@endOfTheDay
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.time

fun Date.startOfTomorrow(): Date =
    Calendar.getInstance().apply {
        time = this@startOfTomorrow
        add(Calendar.DATE, 1)
    }.time.startOfTheDay()

fun Date.endOfTomorrow(): Date =
    Calendar.getInstance().apply {
        time = this@endOfTomorrow
        add(Calendar.DATE, 1)
    }.time.endOfTheDay()

fun Date.startOfTheWeek(): Date =
    Calendar.getInstance().apply {
        time = this@startOfTheWeek
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }.time.startOfTheDay()

fun Date.endOfTheWeek(): Date =
    Calendar.getInstance().apply {
        time = this@endOfTheWeek
        set(Calendar.DAY_OF_WEEK, (firstDayOfWeek - 1 + 6) % 7 + 1)
    }.time.endOfTheDay()

fun Date.monthsAgo(value: Int): Date =
    Calendar.getInstance().apply {
        time = this@monthsAgo
        add(Calendar.MONTH, -value)
    }.time

fun Date.year(): Int =
    Calendar.getInstance().apply {
        time = this@year
    }.get(Calendar.YEAR)

fun Date.month(): Int =
    Calendar.getInstance().apply {
        time = this@month
    }.get(Calendar.MONTH)

fun Date.day(): Int =
    Calendar.getInstance().apply {
        time = this@day
    }.get(Calendar.DAY_OF_MONTH)

fun Date.hours(): Int =
    Calendar.getInstance().apply {
        time = this@hours
    }.get(Calendar.HOUR_OF_DAY)

fun Date.minutes(): Int =
    Calendar.getInstance().apply {
        time = this@minutes
    }.get(Calendar.MINUTE)

fun Date.isToday(): Boolean = Date().let { now -> year() == now.year() && month() == now.month() && day() == now.day() }

fun Date.isYesterday(): Boolean {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time
    return year() == yesterday.year() && month() == yesterday.month() && day() == yesterday.day()
}

fun Date.isThisWeek(): Boolean {
    val now = Date()
    return this in now.startOfTheWeek()..now.endOfTheWeek()
}

fun Date.isThisMonth(): Boolean = Date().let { now -> year() == now.year() && month() == now.month() }

fun Date.isThisYear(): Boolean = Date().let { now -> year() == now.year() }
