/*
 * Infomaniak Core - Android
 * Copyright (C) 2024-2025 Infomaniak Network SA
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.infomaniak.core.compose.basicbutton.BasicButton
import com.infomaniak.core.compose.preview.PreviewLargeWindow
import com.infomaniak.core.compose.preview.PreviewSmallWindow

@Composable
fun BottomStickyButtonScaffold(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    maxPaneWidth: Dp = LocalScaffoldTheme.current.singlePaneMaxWidth,
    stackedButtonVerticalPadding: Dp = LocalScaffoldTheme.current.stackedButtonVerticalPadding,
    containerColor: Color = MaterialTheme.colorScheme.background,
    topBar: @Composable () -> Unit,
    topButton: @Composable ((Modifier) -> Unit)? = null,
    bottomButton: @Composable ((Modifier) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    SinglePaneScaffold(
        modifier = modifier,
        maxPaneWidth = maxPaneWidth,
        containerColor = containerColor,
        snackbarHost = { snackbarHostState?.let { SnackbarHost(hostState = it) } },
        topBar = topBar,
    ) { contentPaddings ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPaddings),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.weight(1.0f), content = content)
            DoubleStackedButtonScaffold(
                modifier = Modifier.padding(vertical = stackedButtonVerticalPadding),
                maxPaneWidth = maxPaneWidth,
                topButton = topButton,
                bottomButton = bottomButton,
            )
        }
    }
}

@PreviewSmallWindow
@PreviewLargeWindow
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun Preview() {
    MaterialTheme {
        CompositionLocalProvider(LocalScaffoldTheme provides ScaffoldThemeDefault.theme(), {
            Surface {
                BottomStickyButtonScaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(),
                            title = {
                                Text(text = "Title")
                            }
                        )
                    },
                    topButton = { modifier ->
                        BasicButton(modifier = modifier, onClick = {}) {
                            Text("Top Button")
                        }
                    },
                    bottomButton = { modifier ->
                        BasicButton(modifier = modifier, onClick = {}) {
                            Text("Bottom Button")
                        }
                    },
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        text = "content",
                        textAlign = TextAlign.Center,
                    )
                }
            }
        })
    }
}
