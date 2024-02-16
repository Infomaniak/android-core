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
    @SerializedName("min_version")
    var minimalAcceptedVersion: String,
) {

    enum class Store(val apiValue: String) {
        PLAY_STORE("google-play-store"),
        FDROID("f-droid")
    }

    enum class Platform(val apiValue: String) {
        ANDROID("android")
    }

    fun mustRequireUpdate(currentVersion: String): Boolean {

        fun String.toVersionNumbers() = split(".").map(String::toInt)

        runCatching {

            val currentVersionNumbers = currentVersion.toVersionNumbers()
            val minimalAcceptedVersionNumbers = minimalAcceptedVersion.toVersionNumbers()

            currentVersionNumbers.forEachIndexed { index, currentNumber ->
                val minimalAcceptedNumber = minimalAcceptedVersionNumbers[index]
                if (currentNumber == minimalAcceptedNumber) return@forEachIndexed

                return currentNumber < minimalAcceptedNumber
            }
        }.onFailure { exception ->
            Sentry.withScope { scope ->
                scope.level = SentryLevel.ERROR
                scope.setExtra("Version from API", minimalAcceptedVersion)
                scope.setExtra("Current Version", currentVersion)
                Sentry.captureException(exception)
            }
        }

        return false
    }
}
