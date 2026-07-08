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
}

android {
    namespace = "com.infomaniak.core.twofactorauth.front"

    buildFeatures {
        compose = true
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    api(project(":TwoFactorAuth:Back"))

    implementation(project(":Common"))
    implementation(project(":Avatar"))
    implementation(project(":Coil"))
    implementation(project(":Network"))
    implementation(project(":Notifications"))
    implementation(project(":Ui:Compose:BasicButton"))
    implementation(project(":Ui:Compose:Basics"))
    implementation(project(":Ui:Compose:Margin"))

    implementation(core.androidx.core.ktx)

    // Compose
    implementation(core.coil.compose)
    implementation(core.coil.network.okhttp)
    implementation(platform(core.compose.bom))
    api(core.compose.runtime)
    implementation(core.compose.ui.android)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.material.icons)
    api(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
}
