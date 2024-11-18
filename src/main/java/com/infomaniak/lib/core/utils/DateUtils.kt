/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val FORMAT_DATE_CLEAR_MONTH = "dd MMM yyyy"
const val FORMAT_DATE_CLEAR_FULL_MONTH = "EEEE d MMMM yyyy"
const val FORMAT_DATE_DAY_MONTH = "EEE d MMM"
const val FORMAT_DATE_DAY_MONTH_YEAR = "EEE d MMM yyyy"
const val FORMAT_DATE_DAY_FULL_MONTH_WITH_TIME = "EEEE d MMMM HH:mm"
const val FORMAT_DATE_DAY_FULL_MONTH_YEAR_WITH_TIME = "EEEE d MMMM yyyy HH:mm"
const val FORMAT_DATE_CLEAR_MONTH_DAY_ONE_CHAR = "d MMM yyyy"
const val FORMAT_DATE_DEFAULT = "dd.MM.yy"
const val FORMAT_DATE_HOUR_MINUTE = "HH:mm"
const val FORMAT_DATE_SHORT_DAY_ONE_CHAR = "d MMM"
const val FORMAT_DATE_WITH_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ssZ"
const val FORMAT_EVENT_DATE = "dd/MM/yyyy HH:mm"
const val FORMAT_FULL_DATE = "EEEE dd MMMM yyyy"
const val FORMAT_FULL_DATE_WITH_HOUR = "EEEE MMM d yyyy HH:mm:ss"
const val FORMAT_NEW_FILE = "yyyyMMdd_HHmmss"

fun Date.format(pattern: String = FORMAT_DATE_DEFAULT): String {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Date.formatWithLocal(formatData: FormatData, formatStyle: FormatStyle, formatStyleSecondary: FormatStyle? = null): String {
    val formatter = when (formatData) {
        FormatData.DATE -> DateTimeFormatter.ofLocalizedDate(formatStyle)
        FormatData.HOUR -> DateTimeFormatter.ofLocalizedTime(formatStyle)
        FormatData.BOTH -> DateTimeFormatter.ofLocalizedDateTime(formatStyle, formatStyleSecondary ?: formatStyle)
    }

    return toInstant().atZone(ZoneId.systemDefault()).format(formatter)
}

enum class FormatData {
    DATE,
    HOUR,
    BOTH,
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

fun Date.setDay(day: Int): Date = Calendar.getInstance().apply {
    time = this@setDay
    set(Calendar.DAY_OF_MONTH, day)
}.time

fun Date.hours(): Int =
    Calendar.getInstance().apply {
        time = this@hours
    }.get(Calendar.HOUR_OF_DAY)

fun Date.setHour(hour: Int): Date = Calendar.getInstance().apply {
    time = this@setHour
    set(Calendar.HOUR_OF_DAY, hour)

    this@setHour.time = time.time
}.time

fun Date.minutes(): Int =
    Calendar.getInstance().apply {
        time = this@minutes
    }.get(Calendar.MINUTE)

fun Date.setMinute(minute: Int): Date = Calendar.getInstance().apply {
    time = this@setMinute
    set(Calendar.MINUTE, minute)
}.time

fun Date.isSameDayAs(targetDate: Date): Boolean {
    return year() == targetDate.year() &&
            month() == targetDate.month() &&
            day() == targetDate.day()
}

fun Date.addDays(amount: Int): Date = Calendar.getInstance().apply {
    time = this@addDays
    add(Calendar.DATE, amount)
}.time

fun Date.isInTheFuture(): Boolean = after(Date())

fun Date.isToday(): Boolean = isSameDayAs(Date())

fun Date.isYesterday(): Boolean {
    val yesterday = Date().addDays(-1)
    return isSameDayAs(yesterday)
}

fun Date.isThisWeek(): Boolean {
    val now = Date()
    return this in now.startOfTheWeek()..now.endOfTheWeek()
}

fun Date.isWeekend(): Boolean = this.day() in 6..7

fun Date.isThisMonth(): Boolean = Date().let { now -> year() == now.year() && month() == now.month() }

fun Date.isThisYear(): Boolean = Date().let { now -> year() == now.year() }

fun Date.roundUpToNextFiveMinutes(): Date = Calendar.getInstance().apply {
    time = this@roundUpToNextFiveMinutes

    val minutesToAdd = 5 - (get(Calendar.MINUTE) % 5)

    add(Calendar.MINUTE, minutesToAdd)
    set(Calendar.SECOND, 0)
}.time

fun Date.getTimeAtHour(hour: Int): Date = Calendar.getInstance().apply {
    time = this@getTimeAtHour

    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.time

fun Date.getMorning(): Date = this.getTimeAtHour(8)

fun Date.getAfternoon(): Date = this.getTimeAtHour(14)

fun Date.getEvening(): Date = this.getTimeAtHour(18)

fun Date.getTomorrow(): Date = Calendar.getInstance().apply {
    time = this@getTomorrow
    add(Calendar.DAY_OF_YEAR, 1)
}.time

fun Date.getNextMonday(): Date = Calendar.getInstance().apply {
    time = this@getNextMonday

    val daysToAdd = (Calendar.MONDAY - get(Calendar.DAY_OF_WEEK) + 7) % 7
    if (daysToAdd == 0) {
        add(Calendar.DAY_OF_YEAR, 7)
    } else {
        add(Calendar.DAY_OF_YEAR, daysToAdd)
    }
}.time
