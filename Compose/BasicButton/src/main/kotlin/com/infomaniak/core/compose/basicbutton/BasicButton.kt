/*
 * Infomaniak SwissTransfer - Android
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Most basic and customizable button component we can share across multiple apps.
 *
 * The button adds a "loader" logic to the default material button. Loading behaviors can be controlled through
 * [showIndeterminateProgress] and [progress].
 *
 * Specifying a progress has the priority over specifying showIndeterminateProgress.
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
    progress: (() -> Float)? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit,
) {
    val isProgressing by remember(progress) { derivedStateOf { showIndeterminateProgress() || progress != null } }
    val isEnabled = enabled() && isProgressing.not()

    val buttonColors = if (isProgressing) colors.applyEnabledColorsToDisabled() else colors
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
        when {
            progress != null -> {
                KeepButtonSize(content) {
                    CircularProgressIndicator(
                        modifier = getProgressModifier(),
                        color = progressColors.progressColor,
                        trackColor = progressColors.trackColor,
                        progress = progress,
                    )
                }
            }
            showIndeterminateProgress() -> {
                KeepButtonSize(content) {
                    CircularProgressIndicator(
                        modifier = getProgressModifier(),
                        color = progressColors.progressColor,
                        trackColor = progressColors.trackColor,
                    )
                }
            }
            else -> content()
        }
    }
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
