/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.core.ui.compose.contactcard

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse

@Immutable
data class ContactCardColors(
    val waveBackground: Color,
    val background: Color,
)

object ContactCardDefaults {
    @Composable
    fun colors(
        waveBackground: Color = Color.Unspecified,
        background: Color = Color.Unspecified,
    ): ContactCardColors {
        val defaultWave = MaterialTheme.colorScheme.surfaceContainerLowest
        val defaultBackground = MaterialTheme.colorScheme.primaryContainer
        return ContactCardColors(
            waveBackground = waveBackground.takeOrElse { defaultWave },
            background = background.takeOrElse { defaultBackground },
        )
    }
}
