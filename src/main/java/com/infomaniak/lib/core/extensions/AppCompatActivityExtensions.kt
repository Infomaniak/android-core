/*
 * Infomaniak kDrive - Android
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
package com.infomaniak.lib.core.extensions

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import java.util.Locale

val acceptedLocale = arrayOf("fr", "de", "it", "en", "es")
val defaultLocale: Locale = Locale.ENGLISH

fun AppCompatActivity.setDefaultLocalIfNeeded() {
    if (nothingLocaleAccepted()) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(defaultLocale))
    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && !AppCompatDelegate.getApplicationLocales().isEmpty) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }
}

private fun AppCompatActivity.nothingLocaleAccepted(): Boolean {
    val systemLocales = LocaleManagerCompat.getSystemLocales(this)

    for (i in 0..systemLocales.size()) {
        if (systemLocales[i]?.language in acceptedLocale) return false
    }
    return true
}