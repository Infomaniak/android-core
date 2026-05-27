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
@file:OptIn(ExperimentalMediaQueryApi::class)

package com.infomaniak.core.ui.compose.windowinset

import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.UiMediaScope
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

fun UiMediaScope.isWindowWidthLarge(): Boolean {
    return windowWidth > WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND.dp
}

fun UiMediaScope.isWindowHeightLarge(): Boolean {
    return windowHeight > WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND.dp
}

fun UiMediaScope.isWindowLarge(): Boolean {
    return isWindowWidthLarge() && isWindowHeightLarge()
}

fun UiMediaScope.isWindowWidthMedium(): Boolean {
    return windowWidth > WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp && !isWindowWidthLarge()
}

fun UiMediaScope.isWindowHeightMedium(): Boolean {
    return windowHeight > WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND.dp && !isWindowHeightLarge()
}

fun UiMediaScope.isWindowMedium(): Boolean {
    return isWindowWidthMedium() && isWindowHeightMedium()
}

fun UiMediaScope.isWindowWidthSmall(): Boolean {
    return windowWidth < WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp
}

fun UiMediaScope.isWindowSmall(): Boolean {
    return isWindowWidthSmall()
}
