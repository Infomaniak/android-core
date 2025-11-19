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
package com.infomaniak.core.ui.compose.bottomstickybuttonscaffolds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.core.ui.compose.basicbutton.BasicButton
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ui.compose.preview.PreviewAllWindows

private val WIDTH_THRESHOLD = 500.dp

@Composable
fun ColumnScope.DoubleStackedButtonScaffold(
    modifier: Modifier = Modifier,
    maxPaneWidth: Dp = LocalScaffoldTheme.current.singlePaneMaxWidth,
    topButton: @Composable ((Modifier) -> Unit)? = null,
    bottomButton: @Composable ((Modifier) -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = maxPaneWidth)
            .align(Alignment.CenterHorizontally),
    ) {
        when {
            topButton == null && bottomButton == null -> Unit
            topButton != null && bottomButton != null -> {
                if (maxWidth < WIDTH_THRESHOLD) {
                    VerticallyStackedButtons(topButton, bottomButton)
                } else {
                    HorizontallyStackedButtons(topButton, bottomButton)
                }
            }
            else -> SingleButton(
                maxWidth = maxPaneWidth / 2,
                button = topButton ?: bottomButton!!
            )
        }
    }
}

@Composable
private fun VerticallyStackedButtons(
    topButton: @Composable (Modifier) -> Unit,
    bottomButton: @Composable (Modifier) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        topButton(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin.Medium),
        )

        Spacer(Modifier.height(Margin.Medium))

        bottomButton(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin.Medium),
        )
    }
}

@Composable
private fun HorizontallyStackedButtons(
    topButton: @Composable (Modifier) -> Unit,
    bottomButton: @Composable (Modifier) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin.Medium),
        horizontalArrangement = Arrangement.spacedBy(Margin.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        topButton(Modifier.weight(1.0f))
        bottomButton(Modifier.weight(1.0f))
    }
}

@Composable
private fun SingleButton(
    maxWidth: Dp,
    button: @Composable (Modifier) -> Unit
) {
    Box(Modifier.widthIn(max = maxWidth)) {
        button(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin.Medium),
        )
    }
}

@PreviewAllWindows
@Composable
private fun DoubleButtonComboPreview() {
    MaterialTheme {
        CompositionLocalProvider(LocalScaffoldTheme provides ScaffoldThemeDefault.theme(), {
            Column {
                DoubleStackedButtonScaffold(
                    topButton = {
                        BasicButton(
                            modifier = it,
                            onClick = {},
                        ) {
                            Text("Top button")
                        }
                    },
                    bottomButton = {
                        BasicButton(
                            modifier = it,
                            onClick = {},
                        ) {
                            Text("Bottom button")
                        }
                    },
                )
                Spacer(Modifier.height(Margin.Medium))
                DoubleStackedButtonScaffold(
                    bottomButton = {
                        BasicButton(
                            modifier = it,
                            onClick = {},
                        ) {
                            Text("Single button")
                        }
                    },
                )
            }
        })
    }
}
