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

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidDebugSigningPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        require(pluginManager.hasPlugin("com.android.application")) {
            "The com.android.application plugin must be applied before com.infomaniak.android.debug-signing"
        }

        val debugKeystore = materializeDebugKeystore()
        extensions.configure<ApplicationExtension> {
            signingConfigs.named("debug") {
                storeFile = debugKeystore
                storePassword = DEBUG_KEYSTORE_PASSWORD
                keyAlias = DEBUG_KEY_ALIAS
                keyPassword = DEBUG_KEY_PASSWORD
            }
        }
    }

    private fun Project.materializeDebugKeystore() = rootProject.layout.projectDirectory
        .file(".gradle/infomaniak/$DEBUG_KEYSTORE_RESOURCE")
        .asFile
        .also { outputFile ->
            val keystoreBytes = readDebugKeystore()

            if (!outputFile.exists() || !outputFile.readBytes().contentEquals(keystoreBytes)) {
                check(outputFile.parentFile.mkdirs() || outputFile.parentFile.isDirectory) {
                    "Could not create shared debug keystore directory: ${outputFile.parentFile}"
                }
                outputFile.writeBytes(keystoreBytes)
            }
        }

    private fun readDebugKeystore(): ByteArray = checkNotNull(javaClass.getResourceAsStream("/$DEBUG_KEYSTORE_RESOURCE")) {
        "Missing shared debug keystore resource: $DEBUG_KEYSTORE_RESOURCE"
    }.use { it.readBytes() }

    private companion object {
        const val DEBUG_KEYSTORE_RESOURCE = "infomaniak-debug.keystore"
        const val DEBUG_KEYSTORE_PASSWORD = "infomaniak-debug"
        const val DEBUG_KEY_ALIAS = "infomaniak-debug"
        const val DEBUG_KEY_PASSWORD = "infomaniak-debug"
    }
}
