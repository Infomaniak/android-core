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
package com.infomaniak.core.application

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.io.File

/**
 * Configures the shared debug signing certificate for Android application modules.
 */
class InfomaniakApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val keystoreFile = listOf(
            target.rootProject.file("debug.keystore"),
            target.rootProject.file("Core/config/debug.keystore"),
            target.rootProject.file("config/debug.keystore"),
        ).firstOrNull(File::exists)

        requireNotNull(keystoreFile) {
            "Shared debug keystore not found. Expected it under <rootProject>/debug.keystore or Core/config/debug.keystore."
        }

        target.rootProject.allprojects {
            plugins.withId("com.android.application") {
                extensions.configure<CommonExtension<*, *, *, *, *, *>> ("android") {
                    signingConfigs {
                        getByName("debug") {
                            storeFile = keystoreFile
                        }
                    }
                }
            }
        }
    }
}
