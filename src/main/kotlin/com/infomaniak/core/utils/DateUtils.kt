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
package com.infomaniak.core.utils

import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val FORMAT_DATE_CLEAR_FULL_MONTH = "EEEE d MMMM yyyy"
const val FORMAT_DATE_CLEAR_MONTH = "dd MMM yyyy"
const val FORMAT_DATE_CLEAR_MONTH_DAY_ONE_CHAR = "d MMM yyyy"
const val FORMAT_DATE_DAY_FULL_MONTH_WITH_TIME = "EEEE d MMMM HH:mm"
const val FORMAT_DATE_DAY_FULL_MONTH_YEAR_WITH_TIME = "EEEE d MMMM yyyy HH:mm"
const val FORMAT_DATE_DAY_MONTH = "EEE d MMM"
const val FORMAT_DATE_DAY_MONTH_YEAR = "EEE d MMM yyyy"
const val FORMAT_DATE_DEFAULT = "dd.MM.yy"
const val FORMAT_DATE_FULL = "EEEE d MMMM"
const val FORMAT_DATE_SHORT_DAY_ONE_CHAR = "d MMM"
const val FORMAT_DATE_SIMPLE = "dd/MM/yyyy"
const val FORMAT_DATE_TITLE = "E d MMMM"
const val FORMAT_DATE_WITH_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ssZ"
const val FORMAT_ISO_8601_WITH_TIMEZONE_SEPARATOR = "yyyy-MM-dd'T'HH:mm:ssXXX"
const val FORMAT_EVENT_DATE = "dd/MM/yyyy HH:mm"
const val FORMAT_FULL_DATE = "EEEE dd MMMM yyyy"
const val FORMAT_FULL_DATE_WITH_HOUR = "EEEE MMM d yyyy HH:mm:ss"
const val FORMAT_HOUR_MINUTES = "HH:mm"
const val FORMAT_NEW_FILE = "yyyyMMdd_HHmmss"

const val SECONDS_IN_A_DAY = 86_400L

//region Format Dates
enum class FormatData {
    DATE,
    HOUR,
    BOTH,
}

fun Date.format(pattern: String = FORMAT_DATE_DEFAULT): String = SimpleDateFormat(pattern, Locale.getDefault()).format(this)

@RequiresApi(26)
fun Date.formatWithLocal(formatData: FormatData, formatStyle: FormatStyle, formatStyleSecondary: FormatStyle? = null): String {
    val formatter = when (formatData) {
        FormatData.DATE -> DateTimeFormatter.ofLocalizedDate(formatStyle)
        FormatData.HOUR -> DateTimeFormatter.ofLocalizedTime(formatStyle)
        FormatData.BOTH -> DateTimeFormatter.ofLocalizedDateTime(formatStyle, formatStyleSecondary ?: formatStyle)
    }

    return toInstant().atZone(ZoneId.systemDefault()).format(formatter)
}
//endregion

//region Get a new Date relatively to a given Date
fun Date.yesterday(): Date = addDays(-1)

fun Date.tomorrow(): Date = addDays(1)

fun Date.startOfTheDay(): Date = Calendar.getInstance().apply {
    time = this@startOfTheDay
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
}.time

fun Date.endOfTheDay(): Date = Calendar.getInstance().apply {
    time = this@endOfTheDay
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
}.time

fun Date.startOfTomorrow(): Date = tomorrow().startOfTheDay()

fun Date.endOfTomorrow(): Date = tomorrow().endOfTheDay()

fun Date.startOfTheWeek(): Date = Calendar.getInstance().apply {
    time = this@startOfTheWeek
    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
}.time.startOfTheDay()

fun Date.endOfTheWeek(): Date = Calendar.getInstance().apply {
    time = this@endOfTheWeek
    set(Calendar.DAY_OF_WEEK, (firstDayOfWeek - 1 + 6) % 7 + 1)
}.time.endOfTheDay()

fun Date.monthsAgo(value: Int): Date = Calendar.getInstance().apply {
    time = this@monthsAgo
    add(Calendar.MONTH, -value)
}.time

fun Date.addYears(years: Int): Date = Calendar.getInstance().apply {
    time = this@addYears
    add(Calendar.YEAR, years)
}.time

fun Date.addDays(amount: Int): Date = Calendar.getInstance().apply {
    time = this@addDays
    add(Calendar.DATE, amount)
}.time

fun Date.setDay(day: Int): Date = Calendar.getInstance().apply {
    time = this@setDay
    set(Calendar.DAY_OF_MONTH, day)
}.time

fun Date.setHour(hour: Int): Date = Calendar.getInstance().apply {
    time = this@setHour
    set(Calendar.HOUR_OF_DAY, hour)
}.time

fun Date.setMinute(minute: Int): Date = Calendar.getInstance().apply {
    time = this@setMinute
    set(Calendar.MINUTE, minute)
}.time

fun Date.roundUpToNextTenMinutes(): Date = Calendar.getInstance().apply {
    time = this@roundUpToNextTenMinutes

    val currentMinute = get(Calendar.MINUTE)
    val minutesToAdd = when {
        currentMinute % 5 == 0 -> 10
        else -> 5 - (currentMinute % 5) + 10
    }

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

fun Date.getNextMonday(): Date = Calendar.getInstance().apply {
    time = this@getNextMonday

    val daysToAdd = (Calendar.MONDAY - get(Calendar.DAY_OF_WEEK) + 7) % 7
    if (daysToAdd == 0) {
        add(Calendar.DAY_OF_YEAR, 7)
    } else {
        add(Calendar.DAY_OF_YEAR, daysToAdd)
    }
}.time
//endregion

//region Get specific data about a given Date
fun Date.year(): Int = Calendar.getInstance().apply {
    time = this@year
}.get(Calendar.YEAR)

fun Date.month(): Int = Calendar.getInstance().apply {
    time = this@month
}.get(Calendar.MONTH)

fun Date.day(): Int = Calendar.getInstance().apply {
    time = this@day
}.get(Calendar.DAY_OF_MONTH)

fun Date.hours(): Int = Calendar.getInstance().apply {
    time = this@hours
}.get(Calendar.HOUR_OF_DAY)

fun Date.minutes(): Int = Calendar.getInstance().apply {
    time = this@minutes
}.get(Calendar.MINUTE)
//endregion

//region Various checks about a given Date
fun Date.isInTheFuture(): Boolean = after(Date())

fun Date.isAtLeastXMinutesInTheFuture(minutes: Int): Boolean {
    val dateTenMinutesLater = Calendar.getInstance().apply {
        time = Date()
        add(Calendar.MINUTE, minutes)
    }.time

    return after(dateTenMinutesLater)
}

fun Date.isSameDayAs(targetDate: Date): Boolean {
    return year() == targetDate.year() &&
            month() == targetDate.month() &&
            day() == targetDate.day()
}

fun Date.isYesterday(): Boolean = isSameDayAs(Date().yesterday())

fun Date.isToday(): Boolean = isSameDayAs(Date())

fun Date.isTomorrow(): Boolean = isSameDayAs(Date().tomorrow())

fun Date.isWeekend(): Boolean {
    val calendar = Calendar.getInstance().apply {
        time = this@isWeekend
    }
    val day = calendar.get(Calendar.DAY_OF_WEEK)
    return day == 1 || day == 7
}

fun Date.isThisWeek(): Boolean = Date().let { now -> this in now.startOfTheWeek()..now.endOfTheWeek() }

fun Date.isThisMonth(): Boolean = Date().let { now -> year() == now.year() && month() == now.month() }

fun Date.isThisYear(): Boolean = Date().let { now -> year() == now.year() }
//endregion
