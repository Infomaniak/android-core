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
    namespace = "com.infomaniak.core.crossapplogin.back"

    buildFeatures.buildConfig = true
}

dependencies {
    api(core.kotlinx.coroutines.core)
    api(core.androidx.lifecycle.runtime.ktx)
    api(core.androidx.lifecycle.service)
    api(core.kotlinx.serialization.protobuf)
    api(core.androidx.work.runtime)

    implementation(project(":Common"))
    implementation(project(":AppIntegrity"))
    implementation(project(":Auth"))
    implementation(project(":Network:Ktor"))
    implementation(project(":Sentry"))
    implementation(core.splitties.mainthread)
    implementation(core.ktor.client.core)
    implementation(core.ktor.client.okhttp)
    implementation(core.ktor.client.json)
    implementation(core.ktor.client.content.negociation)

    implementation(core.androidx.lifecycle.viewmodel.ktx)
    implementation(core.activity.compose) // To access ComponentActivity

    testImplementation(core.junit)
    androidTestImplementation(core.androidx.junit)
}
