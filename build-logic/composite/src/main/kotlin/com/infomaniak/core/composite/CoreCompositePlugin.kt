package com.infomaniak.core.composite

import org.gradle.api.Plugin
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import java.io.File

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
                            println("Substituting $req -> project($projectPath)")

                            useTarget(project(projectPath))
                        }
                    }
                }
            }
        }
    }
}
