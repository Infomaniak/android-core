/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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

plugins {
    alias(core.plugins.infomaniak.android.library.flavor.aware)
    alias(core.plugins.kotlin.serialization)
}

android {
    namespace = "com.infomaniak.core.appintegrity"

    buildTypes {
        release {
            buildConfigField("String", "APP_INTEGRITY_BASE_URL", "\"https://api.infomaniak.com\"")
        }

        debug {
            buildConfigField("String", "APP_INTEGRITY_BASE_URL", "\"https://api.preprod.dev.infomaniak.ch\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(project(":Common"))
    implementation(project(":Sentry"))

    "standardImplementation"(core.integrity)
    "standardImplementation"(core.kotlinx.coroutines.play.services)
    implementation(core.ktor.client.core)
    implementation(core.ktor.client.content.negociation)
    implementation(core.ktor.client.json)
    implementation(core.ktor.client.encoding)
    implementation(core.ktor.client.okhttp)
    implementation(core.kotlinx.serialization.json)
    testImplementation(core.junit)
    testImplementation(core.ktor.client.mock)
    androidTestImplementation(core.androidx.junit)
}
