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
package com.infomaniak.core.twofactorauth.front.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.basicbutton.BasicButton
import com.infomaniak.core.compose.basicbutton.BasicButtonDelay
import com.infomaniak.core.compose.basics.Typography
import kotlin.time.Duration

@Composable
internal fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
    BasicButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        elevation = elevation,
        border = border,
        enabled = enabled,
        showIndeterminateProgress = showIndeterminateProgress,
        indeterminateProgressDelay = indeterminateProgressDelay,
        progress = progress,
        contentPadding = contentPadding,
    ) {
        ProvideTextStyle(Typography.bodyMedium) {
            content()
        }
    }
}
