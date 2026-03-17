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
package com.infomaniak.core.compose.lint

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Automatically adds the lint to all modules defined in the whole project and use Core/lint.xml as the single source of truth to
 * configure the lints in every app.
 */
class ComposeLintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val coreVersionCatalog = target.extensions.getByType<VersionCatalogsExtension>().named("core")
        val lintLibrary = coreVersionCatalog.findLibrary("compose-lint-checks").get()

        target.subprojects {
            plugins.withId("com.android.base") {
                extensions.configure<CommonExtension<*, *, *, *, *, *>>("android") {
                    lint {
                        lintConfig = rootProject.file("Core/lint.xml")

                        // Update the baseline for all the subprojects with `./gradlew updateLintBaseline`
                        // For Core, update the baseline with `./gradlew -p Core updateLintBaseline`
                        baseline = file("lint-baseline.xml")

                        // To updateLintBaseline correctly, temporarily uncomment this line to only include errors and not
                        // warnings in the generated baseline
                        // ignoreWarnings = true
                    }
                }

                dependencies {
                    add("lintChecks", lintLibrary)
                }
            }
        }
    }
}
