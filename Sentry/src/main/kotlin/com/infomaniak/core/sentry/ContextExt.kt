/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.sentry

import android.content.Context
import android.provider.Settings.Global

internal fun Context.getFlavor(): String = runCatching {
    val buildConfigClass = Class.forName("$packageName.BuildConfig")
    val flavorField = buildConfigClass.getField("FLAVOR")
    flavorField[null] as String
}.getOrDefault("standard")

internal fun Context.arePlayServicesLinked(): Boolean {
    val enable = 0
    return runCatching {
        val googleApiAvailabilityClass = Class.forName("com.google.android.gms.common.GoogleApiAvailability")
        val getInstanceMethod = googleApiAvailabilityClass.getDeclaredMethod("getInstance")
        val instance = getInstanceMethod.invoke(null)
        val isAvailableMethod = googleApiAvailabilityClass.getDeclaredMethod("isGooglePlayServicesAvailable", Context::class.java)
        isAvailableMethod.invoke(instance, this) as Int
    }.getOrDefault(1) == enable
}

internal fun Context.isDontKeepActivitiesEnabled(): Boolean {
    val disable = 0
    return Global.getInt(contentResolver, Global.ALWAYS_FINISH_ACTIVITIES, disable) != disable
}
