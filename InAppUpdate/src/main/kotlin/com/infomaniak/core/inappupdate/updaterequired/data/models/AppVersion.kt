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
package com.infomaniak.core.inappupdate.updaterequired.data.models

import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppVersion(
    @SerialName("min_version")
    var minimalAcceptedVersion: String,
    @SerialName("published_versions")
    var publishedVersions: List<AppPublishedVersion>,
) {

    enum class Store(val apiValue: String) {
        PLAY_STORE("google-play-store"),
        FDROID("f-droid")
    }

    enum class Platform(val apiValue: String) {
        ANDROID("android")
    }

    fun mustRequireUpdate(currentVersion: String): Boolean = runCatching {

        val currentVersionNumbers = currentVersion.toVersionNumbers()
        val minimalAcceptedVersionNumbers = minimalAcceptedVersion.toVersionNumbers()

        return isMinimalVersionValid(minimalAcceptedVersionNumbers) &&
                currentVersionNumbers.compareVersionTo(minimalAcceptedVersionNumbers) < 0
    }.getOrElse { exception ->
        Sentry.captureException(exception) { scope ->
            scope.level = SentryLevel.ERROR
            scope.setExtra("Version from API", minimalAcceptedVersion)
            scope.setExtra("Current Version", currentVersion)
        }

        return false
    }

    fun isMinimalVersionValid(minimalVersionNumbers: List<Int>): Boolean {
        val productionVersion = publishedVersions.singleOrNull()?.tag ?: return false
        val productionVersionNumbers = productionVersion.toVersionNumbers()

        return minimalVersionNumbers.compareVersionTo(productionVersionNumbers) <= 0
    }

    companion object {
        fun String.toVersionNumbers() = split(".").map(String::toInt)

        /**
         * Compare Two version in the form of two [List] of [Int].
         * These lists must corresponds to the major, minor and patch version values
         *
         * @return -1 if caller version is older than [other] version, 0 if they are equal, 1 if caller is newer
         */
        fun List<Int>.compareVersionTo(other: List<Int>): Int {

            val selfNormalizedList = normalizeVersionNumbers(other)
            val otherNormalizedList = other.normalizeVersionNumbers(this)

            selfNormalizedList.forEachIndexed { index, currentNumber ->
                val otherNumber = otherNormalizedList[index]
                if (currentNumber == otherNumber) return@forEachIndexed

                return if (currentNumber < otherNumber) -1 else 1
            }

            return 0
        }

        private fun List<Int>.normalizeVersionNumbers(other: List<Int>) = toMutableList().apply {
            while (count() < other.count()) add(0)
        }
    }
}
