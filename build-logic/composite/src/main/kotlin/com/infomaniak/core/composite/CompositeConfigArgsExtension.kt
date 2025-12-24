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

import com.infomaniak.core.composite.CompositeConfigArgsExtension.Companion.EXTENSION_NAME
import org.gradle.api.Action
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware

open class CompositeConfigArgsExtension {

    /**
     * Relative path to the **Core** project root.
     *
     * Default value is `"Core"`.
     *
     * Example:
     * - `"../Core"`
     *
     * This path is used to include the Core project as a composite build and to
     * dynamically substitute `com.infomaniak.core` dependencies with local projects.
     */
    var coreRootPath = "Core"

    companion object Companion {
        internal const val EXTENSION_NAME: String = "coreCompositeConfig"
    }
}

fun Settings.coreCompositeConfig(configure: Action<CompositeConfigArgsExtension>): Unit =
    (this as ExtensionAware).extensions.configure(EXTENSION_NAME, configure)
