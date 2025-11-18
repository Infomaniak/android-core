/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.core.appversionchecker.data.models

import com.infomaniak.core.sentry.SentryLog
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppVersion(
    val id: Int? = null,
    val name: String? = null,
    val platform: String? = null,
    val store: String? = null,
    @SerialName("api_id")
    val apiId: String? = null,
    @SerialName("min_version")
    val minimalAcceptedVersion: String? = null,
    @SerialName("next_version_rate")
    val nextVersionRate: String? = null,
    @SerialName("published_versions")
    val publishedVersions: List<AppPublishedVersion>? = null,
) {

    enum class Store(val apiValue: String) {
        PLAY_STORE("google-play-store"),
        FDROID("f-droid")
    }

    enum class Platform(val apiValue: String) {
        ANDROID("android")
    }

    enum class ProjectionFields(val value: String) {
        MinVersion("min_version"),
        PublishedVersionsTag("published_versions.tag"),
        PublishedVersionType("published_versions.type"),
        PublishedVersionBuildVersion("published_versions.build_version"),
        PublishedVersionMinOs("published_versions.build_min_os_version")
    }

    enum class VersionChannel(val value: String) {
        Production("production"),
        Beta("beta"),
        Internal("internal"),
    }

    fun mustRequireUpdate(currentVersion: String): Boolean = runCatching {
        if (minimalAcceptedVersion == null) {
            SentryLog.d(TAG, "min_version field is empty. Don't forget to use AppVersion.ProjectionFields.MinVersion")
            return false
        }

        val currentVersionNumbers = currentVersion.toVersionNumbers()
        val minimalAcceptedVersionNumbers = minimalAcceptedVersion.toVersionNumbers()

        return isMinimalVersionValid(minimalAcceptedVersionNumbers) &&
                currentVersionNumbers.compareVersionTo(minimalAcceptedVersionNumbers) < 0
    }.getOrElse { exception ->
        SentryLog.e(TAG, exception.message ?: "Exception occurred during app checking", exception) { scope ->
            scope.setExtra("Version from API", minimalAcceptedVersion)
            scope.setExtra("Current Version", currentVersion)
        }

        return false
    }

    fun updateIsAvailable(currentVersion: String, channelFilter: VersionChannel): Boolean = runCatching {
        val currentVersionNumbers = currentVersion.toVersionNumbers()
        val publishedVersionNumber = publishedVersions?.first { it.type == channelFilter.value }?.tag?.toVersionNumbers()

        if (publishedVersionNumber == null) {
            SentryLog.d(TAG, "buildVersion for $channelFilter is null. Verify that the publishedVersions object is present in the response.")
            return false
        }

        return currentVersionNumbers.compareVersionTo(publishedVersionNumber) < 0
    } .getOrElse { exception ->
        SentryLog.e(TAG, exception.message ?: "Exception occurred during app checking $channelFilter version", exception) { scope ->
            scope.setExtra("Version from API", publishedVersions?.first { it.type == channelFilter.value }?.tag)
            scope.setExtra("Current Version", currentVersion)
        }
        
        return false
    }

    fun isMinimalVersionValid(minimalVersionNumbers: List<Int>): Boolean {
        val productionVersion = publishedVersions?.singleOrNull()?.tag ?: return false
        val productionVersionNumbers = productionVersion.toVersionNumbers()

        return minimalVersionNumbers.compareVersionTo(productionVersionNumbers) <= 0
    }

    companion object {
        const val TAG = "AppVersion"

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
