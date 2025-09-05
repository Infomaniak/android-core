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
package com.infomaniak.core.compose.basicbutton

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Most basic and customizable button component we can share across multiple apps.
 *
 * The button adds a "loader" logic to the default material button. Loading behaviors can be controlled through
 * [showIndeterminateProgress] and [progress].
 *
 * Specifying a progress has the priority over specifying showIndeterminateProgress.
 *
 * @param indeterminateProgressDelay How much delay there needs to be before the button changes to the indeterminate progress
 * aspect when its states becomes indeterminate. This is used when code might depend on a slow action (like an API call) and we
 * want to avoid showing the loader if the action ends up being short
 */
@Composable
fun BasicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    enabled: () -> Boolean = { true },
    showIndeterminateProgress: () -> Boolean = { false },
    indeterminateProgressDelay: Duration = BasicButtonDelay.Instantaneous,
    progress: (() -> Float)? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit,
) {
    val uiState by produceUiState(progress, showIndeterminateProgress, indeterminateProgressDelay)

    val isLoading by remember { derivedStateOf { uiState is UiState.Loading } }
    // Previously, we just used `isLoading.not()`, but `produceState` uses a remember, so even when the value of `initialValue` changes,
    // the remember remembers the previous value, meaning that the value of `isEnabled` is not up to date.
    // To fix this problem, we had to duplicate some logic here to ensure that `isEnabled` is always up to date without
    // waiting for the `indeterminateProgressDelay` timeout.
    val isEnabled = enabled() && showIndeterminateProgress().not() && progress == null

    val buttonColors = if (isLoading) colors.applyEnabledColorsToDisabled() else colors
    val progressColors = LoaderColors.fromButtonColors(colors)

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = isEnabled,
        shape = shape,
        colors = buttonColors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
    ) {
        when (val state = uiState) {
            is UiState.Loading.Determinate -> {
                KeepButtonSize(content) {
                    CircularProgressIndicator(
                        modifier = getProgressModifier(),
                        color = progressColors.progressColor,
                        trackColor = progressColors.trackColor,
                        progress = state.progress,
                    )
                }
            }
            UiState.Loading.Indeterminate -> {
                KeepButtonSize(content) {
                    CircularProgressIndicator(
                        modifier = getProgressModifier(),
                        color = progressColors.progressColor,
                        trackColor = progressColors.trackColor,
                    )
                }
            }
            UiState.Default, UiState.Loading.Delayed -> content()
        }
    }
}

@Composable
private fun produceUiState(
    progress: (() -> Float)?,
    showIndeterminateProgress: () -> Boolean,
    indeterminateProgressDelay: Duration
): State<UiState> = produceState(
    initialValue = computeUiState(progress, showIndeterminateProgress, effectiveShowIndeterminate = true),
    key1 = showIndeterminateProgress(),
    key2 = indeterminateProgressDelay,
    key3 = progress,
) {
    val effectiveShowIndeterminate = if (showIndeterminateProgress()) {
        delay(indeterminateProgressDelay)
        true
    } else {
        false
    }

    value = computeUiState(progress, showIndeterminateProgress, effectiveShowIndeterminate)
}

private fun computeUiState(
    progress: (() -> Float)?,
    showIndeterminateProgress: () -> Boolean,
    effectiveShowIndeterminate: Boolean,
): UiState = when {
    progress != null -> UiState.Loading.Determinate(progress)
    showIndeterminateProgress() && effectiveShowIndeterminate -> UiState.Loading.Indeterminate
    showIndeterminateProgress() && effectiveShowIndeterminate.not() -> UiState.Loading.Delayed
    else -> UiState.Default
}

private fun ButtonColors.applyEnabledColorsToDisabled(): ButtonColors {
    return copy(disabledContainerColor = containerColor, disabledContentColor = contentColor)
}

@Composable
private fun KeepButtonSize(targetSizeContent: @Composable () -> Unit, content: @Composable () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Row(modifier = Modifier.alpha(0.0f)) {
            targetSizeContent()
        }
        content()
    }
}

@Composable
private fun getProgressModifier(): Modifier {
    val progressModifier = Modifier
        .heightIn(max = 32.dp)
        .fillMaxHeight(0.8f)
        .aspectRatio(1f)
    return progressModifier
}

private data class LoaderColors(val progressColor: Color, val trackColor: Color) {
    companion object {
        private const val TRACK_COLOR_ALPHA = 0.3f

        fun fromButtonColors(colors: ButtonColors): LoaderColors = LoaderColors(
            progressColor = colors.contentColor,
            trackColor = colors.contentColor.copy(alpha = TRACK_COLOR_ALPHA),
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
                BasicButton(
                    modifier = Modifier.height(40.dp),
                    onClick = {},
                    content = { Text("Click me!") },
                )
                BasicButton(
                    modifier = Modifier.height(40.dp),
                    onClick = {},
                    content = { Text("Click me!") },
                    enabled = { false },
                )
                BasicButton(
                    modifier = Modifier.height(40.dp),
                    onClick = {},
                    progress = { 0.8f },
                    content = { Text("Click me!") },
                )
                BasicButton(
                    modifier = Modifier.height(64.dp),
                    onClick = {},
                    progress = { 0.8f },
                    content = { Text("Click me!") },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

object BasicButtonDelay {
    val Instantaneous = 0.seconds
    val Delayed = 600.milliseconds
}

private sealed interface UiState {
    sealed interface Loading : UiState {
        data class Determinate(val progress: () -> Float) : Loading
        data object Indeterminate : Loading
        data object Delayed : Loading
    }

    data object Default : UiState
}

@Preview
@Composable
private fun PreviewIndeterminateLoading() {
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(2.seconds)
            isLoading = false
        }
    }
    MaterialTheme {
        Surface {
            BasicButton(
                modifier = Modifier.height(40.dp),
                onClick = { isLoading = !isLoading },
                content = { Text("Click me!") },
                showIndeterminateProgress = { isLoading },
                indeterminateProgressDelay = BasicButtonDelay.Delayed,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDeterminateLoading() {
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val steps = 20

    LaunchedEffect(isLoading) {
        if (isLoading) {
            repeat(steps) {
                delay(1.seconds.inWholeMilliseconds / steps)
                progress += 1f / steps
            }
            progress = 0f
            isLoading = false
        }
    }
    MaterialTheme {
        Surface {
            BasicButton(
                modifier = Modifier.height(40.dp),
                onClick = { isLoading = !isLoading },
                content = { Text("Click me!") },
                progress = if (isLoading) fun() = progress else null,
                indeterminateProgressDelay = BasicButtonDelay.Delayed,
            )
        }
    }
}
