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
package com.infomaniak.core.ui.compose.bottomstickybuttonscaffolds

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.core.ui.compose.margin.Margin

@Immutable
data class ScaffoldTheme(
    val singlePaneMaxWidth: Dp = Dp.Unspecified,
    val stackedButtonVerticalPadding: Dp = Dp.Unspecified
)

object ScaffoldThemeDefault {
    fun theme(
        singlePaneMaxWidth: Dp = 800.dp,
        stackedButtonVerticalPadding: Dp = Margin.Small
    ) = ScaffoldTheme(
        singlePaneMaxWidth = singlePaneMaxWidth,
        stackedButtonVerticalPadding = stackedButtonVerticalPadding
    )
}

val LocalScaffoldTheme = staticCompositionLocalOf { ScaffoldThemeDefault.theme() }

