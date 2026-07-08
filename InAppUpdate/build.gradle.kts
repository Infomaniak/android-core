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
    alias(core.plugins.navigation.safeargs)
    alias(core.plugins.kotlin.serialization)
}

android {
    namespace = "com.infomaniak.core.inappupdate"

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(project(":Common"))
    implementation(project(":AppVersionChecker"))
    implementation(project(":Network"))
    implementation(project(":Sentry"))
    implementation(project(":Ui"))
    implementation(project(":Ui:Compose:BottomStickyButtonScaffolds"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Ui:View"))

    implementation(core.androidx.concurrent.futures.ktx)
    implementation(core.androidx.datastore.preferences)
    implementation(core.appcompat)
    implementation(core.androidx.work.runtime)
    implementation(core.kotlinx.serialization.json)

    "standardImplementation"(core.play.app.update)
    "standardImplementation"(core.play.app.update.ktx)

    implementation(core.material)

    implementation(core.navigation.fragment.ktx)
    implementation(core.navigation.ui.ktx)

    implementation(core.okhttp)

    // Compose
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
    implementation(core.androidx.adaptive)
    implementation(core.activity.compose)
}
