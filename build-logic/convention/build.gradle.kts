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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

fun Provider<PluginDependency>.dependencyNotation() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

dependencies {
    compileOnly(core.gradle.build.tools)
    compileOnly(core.plugins.kotlin.android.dependencyNotation())
//    compileOnly(core.plugins.android.library.dependencyNotation())
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register(core.plugins.infomaniak.android.library, "AndroidLibraryConventionPlugin")
        register(core.plugins.infomaniak.android.library.flavor.aware, "FlavorAwareAndroidLibraryConventionPlugin")
    }
}

fun NamedDomainObjectContainer<PluginDeclaration>.register(
    versionCatalogEntry: ProviderConvertible<PluginDependency>,
    className: String,
) = register(versionCatalogEntry = versionCatalogEntry.asProvider(), className = className)

fun NamedDomainObjectContainer<PluginDeclaration>.register(
    versionCatalogEntry: Provider<PluginDependency>,
    className: String,
) {
    val pluginId = versionCatalogEntry.get().pluginId
    val name = pluginId.split('.').joinToString(separator = "") { it.replaceFirstChar { it.uppercaseChar() }}
    register(name) {
        id = pluginId
        implementationClass = className
    }
}
