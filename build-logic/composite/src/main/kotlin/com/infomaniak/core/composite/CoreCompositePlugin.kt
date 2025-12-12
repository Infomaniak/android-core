package com.infomaniak.core.composite

import org.gradle.api.Plugin
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import java.io.File

/**
 * Gradle Settings plugin that wires the local **Core** project as a composite build and substitutes
 * any dependency from the `com.infomaniak.core` group with the corresponding included project.
 *
 * ## Core dependencies (dynamic mapping)
 * Core dependencies are resolved dynamically. To reference a Core module, use:
 * `com.infomaniak.core:Module.Name`
 *
 * To depend on the **Core** module itself, use:
 * `com.infomaniak.core:Core`
 *
 * The `:` separators from the Core project path must be written using **dots** in the artifact name:
 * - `:TwoFactorAuth:Front` → `com.infomaniak.core:TwoFactorAuth.Front`
 *
 * Dots are also used for sub-modules:
 * - `:ModuleA:ModuleB:ModuleC` → `com.infomaniak.core:ModuleA.ModuleB.ModuleC`
 *
 * ## Custom Core root path
 * You can change the relative path to the Core directory by setting [coreRootPath].
 * Default value is `"Core"`.
 *
 * See: [com.infomaniak.core.composite.CompositeConfigArgsExtension.coreRootPath]
 */
class CoreCompositePlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val compositeArgs = target.extensions.create<CompositeConfigArgsExtension>(CompositeConfigArgsExtension.EXTENSION_NAME)
        val alreadyLogged = mutableSetOf<String>()

        target.gradle.settingsEvaluated {
            val coreFile = File(target.settingsDir, compositeArgs.coreRootPath)
            if (coreFile.exists().not()) {
                error(
                    "The Core module directory '${compositeArgs.coreRootPath}' does not exist. \n" +
                            "If you have renamed the Core folder, please update the `coreRootPath` property in your `settings.gradle` file " +
                            "within the `coreCompositeConfig` block. \nExample of usage: \n\n" +
                            "coreCompositeConfig {\n" +
                            "    coreRootPath = \"../CoreInf\"\n" +
                            "}"
                )
            }

            target.includeBuild(compositeArgs.coreRootPath) {
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

            target.project(":Legacy").projectDir = File("${compositeArgs.coreRootPath}/Legacy")
        }
    }
}
