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
    alias(core.plugins.infomaniak.android.library)
}

android {
    namespace = "com.infomaniak.core.common"
}

dependencies {
    api(core.kotlinx.coroutines.core)
    api(core.splitties.appctx)
    api(core.splitties.systemservices)
    api(core.splitties.coroutines)
    api(core.androidx.lifecycle.service)
    api(core.splitties.intents)
    implementation(core.androidx.browser)
    implementation(core.androidx.collection)
    implementation(core.splitties.bitflags)
    implementation(core.splitties.toast)
    implementation(core.splitties.bundle)
    implementation(core.splitties.mainhandler)
    implementation(core.splitties.mainthread)
    implementation(core.androidx.core)
    implementation(core.androidx.core.ktx)
    implementation(core.androidx.lifecycle.runtime.ktx)
    implementation(core.androidx.lifecycle.process)

    testImplementation(kotlin("test"))
    testImplementation(core.kotest.assertions)
    testImplementation(core.kotlinx.coroutines.test)
    testImplementation(core.androidx.junit)
    testImplementation(core.androidx.test.core.ktx)
    testImplementation(core.androidx.test.core)
    testImplementation(core.junit)
    testImplementation(core.robolectric)
}
