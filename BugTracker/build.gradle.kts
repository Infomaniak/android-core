/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
    alias(core.plugins.navigation.safeargs)
}

android {
    namespace = "com.infomaniak.core.bugtracker"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":AppVersionChecker"))
    implementation(project(":Auth"))
    implementation(project(":File"))
    implementation(project(":Network"))
    implementation(project(":Ui"))
    implementation(project(":Ui:View"))

    implementation(core.androidx.core.ktx)
    implementation(core.androidx.lifecycle.viewmodel.ktx)
    implementation(core.androidx.recyclerview)

    implementation(core.navigation.fragment.ktx)
    implementation(core.navigation.ui.ktx)

    implementation(core.kotlinx.coroutines.core)
    implementation(core.okhttp)
}
