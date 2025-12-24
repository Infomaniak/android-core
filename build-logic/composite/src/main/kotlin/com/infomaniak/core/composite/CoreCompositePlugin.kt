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
package com.infomaniak.core.composite

import org.gradle.api.Plugin
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.initialization.ConfigurableIncludedBuild
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import java.io.File

/**
 * Gradle **Settings** plugin that wires the local **Core** project as a composite build and
 * dynamically substitutes any dependency from the `com.infomaniak.core` group with the
 * corresponding included project.
 *
 * ## Core dependencies (dynamic mapping)
 *
 * Core dependencies are resolved dynamically through dependency substitution.
 * To reference a Core module, use:
 *
 * `com.infomaniak.core:Module.Name`
 *
 * To depend on the **Core** root module itself, use a dependency notation like this:
 *
 * `com.infomaniak.core:Core`
 *
 * The `:` separators from the Core project path must be written using **dots** in the artifact name:
 *
 * - `project(":TwoFactorAuth:Front")` → `"com.infomaniak.core:TwoFactorAuth.Front"`
 * - `project(":ModuleA:ModuleB:ModuleC")` → `"com.infomaniak.core:ModuleA.ModuleB.ModuleC"`
 *
 * ---
 *
 * ## Legacy module support
 *
 * The plugin also supports the **Legacy** module located inside the Core repository.
 *
 * To enable it, simply declare the module in the host project's `settings.gradle(.kts)`:
 *
 * ```kotlin
 * include(":Core:Legacy")
 * ```
 *
 * When declared, the plugin automatically remaps the `:Legacy` project to:
 *
 * `${coreRootPath}/Legacy`
 *
 * This allows the Legacy module to be used as a regular Gradle project:
 *
 * ```kotlin
 * dependencies {
 *     implementation(project(":Core:Legacy"))
 * }
 * ```
 *
 * This approach keeps backward compatibility with the historical Legacy module
 * while allowing Core to be consumed through a composite build.
 *
 * ---
 *
 * ## Custom Core root path
 *
 * You can change the relative path to the Core directory by setting [coreRootPath](com.infomaniak.core.composite.CompositeConfigArgsExtension.coreRootPath).
 *
 * Default value is `"Core"`.
 *
 * Example (in `settings.gradle.kts`):
 *
 * ```kotlin
 * coreCompositeConfig {
 *     coreRootPath = "../Core"
 * }
 * ```
 *
 * @see com.infomaniak.core.composite.CompositeConfigArgsExtension.coreRootPath
 */
class CoreCompositePlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val compositeArgs = target.extensions.create<CompositeConfigArgsExtension>(CompositeConfigArgsExtension.EXTENSION_NAME)

        target.gradle.settingsEvaluated {
            val coreFile = File(target.settingsDir, compositeArgs.coreRootPath)
            if (coreFile.exists().not()) {
                error(
                    "The Core module directory '${compositeArgs.coreRootPath}' does not exist. \n" +
                            "If you have renamed the Core folder, please update the `coreRootPath` property in your `settings.gradle` file " +
                            "within the `coreCompositeConfig` block. \nExample of usage: \n\n" +
                            "coreCompositeConfig {\n" +
                            "    coreRootPath = \"../Core\"\n" +
                            "}"
                )
            }

            target.includeBuild(compositeArgs.coreRootPath) {
                // Support for :Core:Legacy
                // Rename include build name to avoid a Gradle name collision with an existing ':Core' project in the main build
                name = "${compositeArgs.coreRootPath}Ik"

                configureCoreDependencySubstitution()
            }
        }
    }

    private fun ConfigurableIncludedBuild.configureCoreDependencySubstitution() {
        val alreadyLogged = mutableSetOf<String>()
        dependencySubstitution {
            all {
                val req = requested
                if (req is ModuleComponentSelector && req.group == "com.infomaniak.core") {
                    val artifact = req.module
                    val projectPath = if (artifact == "Core") ":" else ":${artifact.replace('.', ':')}"

                    if (alreadyLogged.add(req.toString())) {
                        println("Substituting $req -> project($projectPath)")
                    }

                    useTarget(project(projectPath))
                }
            }
        }
    }
}
