/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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

import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(core.plugins.android.library) apply false
    alias(core.plugins.kotlin.android) apply false
    alias(core.plugins.ktlint)
    alias(core.plugins.compose.lint)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(ReporterType.PLAIN)
    }
}
