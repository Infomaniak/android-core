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
    alias(core.plugins.compose.compiler)
    kotlin("plugin.parcelize")
    alias(core.plugins.ksp)
    alias(core.plugins.kotlin.serialization)
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

android {
    namespace = "com.infomaniak.core.auth"

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    api(project(":Login"))
    implementation(project(":Common"))
    implementation(project(":AppIntegrity"))
    implementation(project(":Network"))
    implementation(project(":Sentry"))

    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    implementation(core.compose.ui)
    implementation(core.compose.material3)
    implementation(core.activity.compose)
    implementation(core.compose.ui.tooling.preview)
    debugImplementation(core.compose.ui.tooling)

    implementation(core.appcompat)
    implementation(core.androidx.core.ktx)
    implementation(core.kotlinx.serialization.json)
    implementation(core.gson)
    implementation(core.splitties.appctx)
    implementation(core.okhttp)
    api(core.gson)

    // Room
    implementation(core.room.ktx)
    api(core.room.runtime)
    ksp(core.room.compiler)

    // Test
    testImplementation(core.junit)
    testImplementation(core.androidx.test.core)
    testImplementation(core.kotlinx.coroutines.test)
    testImplementation(core.robolectric)
}
