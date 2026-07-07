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
    alias(core.plugins.compose.compiler)
    alias(core.plugins.navigation.safeargs)
}

android {
    namespace = "com.infomaniak.core.applock"

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(project(":Common"))
    implementation(project(":Ui:Compose:Basics"))
    implementation(project(":Ui:Compose:BottomStickyButtonScaffolds"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Ui:View"))

    implementation(platform(core.compose.bom))
    implementation(core.compose.material3)
    implementation(core.compose.runtime)
    implementation(core.compose.ui.tooling)
    implementation(core.activity.compose)

    implementation(core.appcompat)
    implementation(core.biometric.ktx)
    implementation(core.navigation.fragment.ktx)
    implementation(core.splitties.appctx)
    implementation(core.splitties.mainhandler)
    implementation(core.splitties.systemservices)

    implementation(core.constraintlayout)
    implementation(core.material)
}
