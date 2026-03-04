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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class ComposeLintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.subprojects {
            plugins.withId("com.android.base") {
                extensions.configure<com.android.build.gradle.internal.dsl.BaseAppModuleExtension> {
                    lint {
                        lintConfig = rootProject.file("Core/lint.xml")
                    }
                }

                val coreVersionCatalog = extensions
                    .getByType<VersionCatalogsExtension>()
                    .named("core")

                dependencies {
                    add("lintChecks", coreVersionCatalog.findLibrary("compose-lint-checks").get())
                }
            }
        }
    }
}
