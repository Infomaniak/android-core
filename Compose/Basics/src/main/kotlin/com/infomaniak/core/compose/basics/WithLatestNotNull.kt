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
package com.infomaniak.core.compose.basics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Gives access to the latest non null value of the receiver.
 *
 * Helpful to keep showing populated UI during exit animations.
 */
@Composable
fun <T> T?.WithLatestNotNull(content: @Composable (T) -> Unit) {
    // The implementation is similar to rememberUpdatedState.
    val value by remember { mutableStateOf(this) }.also {
        if (this != null) it.value = this
    }
    value?.let {
        content(it)
    }
}
