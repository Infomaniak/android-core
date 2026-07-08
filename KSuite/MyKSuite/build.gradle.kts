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
    alias(core.plugins.ksp)
    kotlin("plugin.parcelize")
    alias(core.plugins.kotlin.serialization)
    alias(core.plugins.navigation.safeargs)
}

android {
    namespace = "com.infomaniak.core.ksuite.myksuite"

    buildFeatures {
        compose = true
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {

    implementation(project(":Common"))
    implementation(project(":Avatar"))
    implementation(project(":Coil"))
    implementation(project(":KSuite"))
    implementation(project(":Network")) // To access API URL
    implementation(project(":Ui:Compose:Basics"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Ui:Compose:MaterialThemeFromXml"))

    implementation(core.androidx.core.ktx)
    implementation(core.material)
    implementation(core.navigation.fragment.ktx)
    implementation(core.kotlinx.serialization.json)

    implementation(core.coil)
    implementation(core.coil.compose)

    // Room
    implementation(core.room.runtime)
    implementation(core.room.ktx)
    ksp(core.room.compiler)

    // Compose
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
}
