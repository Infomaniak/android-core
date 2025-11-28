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
plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
    kotlin("plugin.serialization")
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {

    namespace = "com.infomaniak.core.twofactorauth.back"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    api(core.kotlinx.coroutines.core)
    api(core.okhttp)

    implementation(project(":Core"))
    implementation(project(":Network"))
    implementation(project(":Core:Notifications"))
    implementation(project(":Sentry"))

    implementation(core.androidx.core.ktx)
    implementation(core.androidx.lifecycle.process)
    implementation(core.kotlinx.serialization.json)
    implementation(core.ktor.client.json)
    implementation(core.ktor.client.content.negociation)
    implementation(core.ktor.client.core)
    implementation(core.ktor.client.okhttp)
}
