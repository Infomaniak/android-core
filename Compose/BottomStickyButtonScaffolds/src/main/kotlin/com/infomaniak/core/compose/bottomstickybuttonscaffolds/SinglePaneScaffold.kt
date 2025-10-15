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
package com.infomaniak.core.compose.bottomstickybuttonscaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.infomaniak.core.compose.preview.PreviewLargeWindow
import com.infomaniak.core.compose.preview.PreviewLightAndDark

@Composable
fun SinglePaneScaffold(
    modifier: Modifier = Modifier,
    maxPaneWidth: Dp = LocalScaffoldTheme.current.singlePaneMaxWidth,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
    ) { contentPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(Modifier.widthIn(max = maxPaneWidth)) {
                content(contentPadding)
            }
        }
    }
}

@PreviewLightAndDark
@PreviewLargeWindow
@Composable
private fun Preview() {
    MaterialTheme {
        CompositionLocalProvider(LocalScaffoldTheme provides ScaffoldThemeDefault.theme()) {
            Surface {
                SinglePaneScaffold {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Cyan)
                    )
                }
            }
        }
    }
}
