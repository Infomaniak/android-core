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
package com.infomaniak.lib.stores.updaterequired.data.models

import com.google.gson.annotations.SerializedName
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.serialization.Serializable

@Serializable
data class AppVersion(
    var id: Int,
    var name: String,
    var platform: Platform,
    var store: Store,
    @SerializedName("api_id")
    var apiId: String,
    @SerializedName("min_version")
    var minVersion: String,
    @Suppress("ArrayInDataClass")
    var publishedVersions: Array<AppPublishedVersion>,
) {

    enum class Store(val apiValue: String) {
        PLAY_STORE("google-play-store"),
        FDROID("f-droid")
    }


    enum class Platform(val apiValue: String) {
        ANDROID("android")
    }

    fun mustRequireUpdate(currentAppVersion: String): Boolean {

        fun String.toVersionNumbers() = split(".").map(String::toInt)

        val currentVersionNumbers = currentAppVersion.toVersionNumbers()
        val minVersionNumbers = minVersion.toVersionNumbers()

        runCatching {
            currentVersionNumbers.forEachIndexed { index, versionNumber ->
                val minVersionNumber = minVersionNumbers[index]
                if (versionNumber == minVersionNumber) return@forEachIndexed

                return versionNumber < minVersionNumber
            }
        }.onFailure { exception ->
            Sentry.withScope { scope ->
                scope.level = SentryLevel.ERROR
                scope.setExtra("Version from API", minVersion)
                scope.setExtra("Current Version", currentAppVersion)
                Sentry.captureException(exception)
            }
        }

        return false
    }
}
