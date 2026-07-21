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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.DerivedTokenGenerator

@Composable
fun RestoringFromBackupFailedScreen(
    state: RestoreFromBackupManager.State.RestoringFromBackupFailed,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Bottom)
    ) {
        Text("Failed to restore your account", textAlign = TextAlign.Center)
        Button(onClick = state.retry) { Text("Retry") }
        TextButton(onClick = state.giveUp) { Text("Give up") }
        Spacer(Modifier.padding(72.dp).navigationBarsPadding())
    }
}

@Preview
@Composable
private fun RestoringFromBackupScreenPreviewScreen() {
    RestoringFromBackupFailedScreen(RestoreFromBackupManager.State.RestoringFromBackupFailed(
        cause = DerivedTokenGenerator.Issue.OtherIssue(Exception()),
        retry = {},
        giveUp = {}
    ))
}
