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
package com.infomaniak.core.dotlottie.model

import androidx.compose.ui.graphics.Color
import com.infomaniak.core.dotlottie.ThemeData
import kotlinx.serialization.json.Json

sealed interface DotLottieTheme {
    val id: String?

    data class Embedded(override val id: String?) : DotLottieTheme
    data class Custom(val colorMapping: Map<String, Color>) : DotLottieTheme {
        override val id: String? = null
        internal fun toData(): String = json.encodeToString(ThemeData(rules = colorMapping.map(ThemeData::Rule)))
    }

    companion object {
        private val json = Json {
            encodeDefaults = true
        }
    }
}
