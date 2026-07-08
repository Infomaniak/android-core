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
    alias(core.plugins.infomaniak.android.library)
    alias(core.plugins.compose.compiler)
}

android {
    namespace = "com.infomaniak.core.filetypes"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":Ui:Compose:Margin"))

    implementation(core.androidx.core.ktx)
    implementation(platform(core.compose.bom))
    implementation(core.compose.foundation)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)

    testImplementation(core.junit)
    androidTestImplementation(core.androidx.junit)
    debugImplementation(core.compose.ui.tooling)
}
