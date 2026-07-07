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

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

fun CommonExtension<*, *, *, *, *, *>.applyCommonConfiguration() {
    compileSdk = 36
    defaultConfig {
        minSdk = 27
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        val javaVersion = JavaVersion.VERSION_11
        targetCompatibility = javaVersion // Generated JVM bytecode
        sourceCompatibility = javaVersion // Java language features. We are not writing Java, so we just keep it matching.
        // https://github.com/google/desugar_jdk_libs/blob/master/CHANGELOG.md
        // https://developer.android.com/studio/write/java11-minimal-support-table
        // https://developer.android.com/studio/write/java8-support
    }
}
