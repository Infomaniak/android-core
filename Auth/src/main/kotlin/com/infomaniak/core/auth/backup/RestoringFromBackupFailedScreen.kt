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
package com.infomaniak.core.auth.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.DerivedTokenGenerator
import com.infomaniak.core.auth.R
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.common.R as RCore

@Composable
fun RestoringFromBackupFailedScreen(
    state: RestoreFromBackupManager.State.RestoringFromBackupFailed,
    modifier: Modifier = Modifier,
) = AspectRatioFlow(modifier) { isLandscape ->
    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(Margin.Medium, alignment = Alignment.CenterHorizontally)
        ) {
            Spacer(Modifier.weight(1f))
            RestorationFailed(
                state = state,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
    } else {
        RestorationFailed(
            state = state,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun RestorationFailed(
    state: RestoreFromBackupManager.State.RestoringFromBackupFailed,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(bottom = Margin.Giant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Margin.Mini, alignment = Alignment.Bottom),
    ) {
        Text(
            stringResource(R.string.accountRestoreFailedError),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Margin.Mini))
        Button(onClick = state.retry) { Text(stringResource(RCore.string.buttonRetry)) }
        TextButton(onClick = state.giveUp) { Text(stringResource(R.string.buttonGiveUp)) }
        Spacer(
            Modifier
                .height(72.dp)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun AspectRatioFlow(
    modifier: Modifier = Modifier,
    content: @Composable (isLandscape: Boolean) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val isLandscape = maxWidth > maxHeight
        content(isLandscape)
    }
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Preview
@Composable
private fun RestoringFromBackupScreenPreviewScreen() {
    RestoringFromBackupFailedScreen(
        RestoreFromBackupManager.State.RestoringFromBackupFailed(
            cause = DerivedTokenGenerator.Issue.OtherIssue(Exception()),
            retry = {},
            giveUp = {}
        ))
}
