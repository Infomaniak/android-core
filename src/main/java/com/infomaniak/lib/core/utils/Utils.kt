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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.CountDownTimer
import androidx.core.os.LocaleListCompat
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.min

object Utils {
    private const val MIN_HEIGHT_FOR_LANDSCAPE = 4

    val ACCENTS_PATTERN: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    val CAMEL_CASE_REGEX = "(?<=[a-z])[A-Z]".toRegex()
    val SNAKE_CASE_REGEX = "_[a-zA-Z]".toRegex()

    fun createRefreshTimer(milliseconds: Long = 600L, onTimerFinish: () -> Unit): CountDownTimer {
        return object : CountDownTimer(milliseconds, milliseconds) {
            override fun onTick(millisUntilFinished: Long) = Unit
            override fun onFinish() {
                onTimerFinish()
            }
        }
    }

    fun getDefaultAcceptedLanguage(): String {
        val acceptedApiLanguage = arrayOf("fr", "de", "it", "en", "es")
        return getPreferredLocaleList().firstOrNull { it.language in acceptedApiLanguage }?.language ?: "en"
    }

    fun getPreferredLocaleList(): List<Locale> {
        val adjustedLocaleListCompat = LocaleListCompat.getAdjustedDefault()
        val preferredLocaleList = mutableListOf<Locale>()
        for (index in 0 until adjustedLocaleListCompat.size()) {
            preferredLocaleList.add(adjustedLocaleListCompat[index]!!)
        }
        return preferredLocaleList
    }

    inline fun <reified T : Enum<T>> enumValueOfOrNull(value: String?): T? {
        return value?.let { runCatching { enumValueOf<T>(it.uppercase()) }.getOrNull() }
    }

    inline fun <reified T : Enum<T>> String.toEnumOrThrow(): T {
        return enumValueOf(uppercase())
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun Activity.lockOrientationForSmallScreens() {
        val (screenHeightInches, screenWidthInches) = with(resources.displayMetrics) { (heightPixels / ydpi) to (widthPixels / xdpi) }

        val aspectRatio = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_LONG_MASK
        val isLongScreen = aspectRatio != Configuration.SCREENLAYOUT_LONG_NO

        val isScreenTooSmall = isLongScreen && min(screenHeightInches, screenWidthInches) < MIN_HEIGHT_FOR_LANDSCAPE

        if (isScreenTooSmall) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
