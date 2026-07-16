/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.core.ui.compose.contactcard.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.common.R as RCore

@Composable
internal fun EditorTopBar(
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    ContactCardTopBar(
        navigationIcon = {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(RCore.string.buttonCancel),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        actions = {
            TextButton(onClick = onSave) {
                Text(
                    text = stringResource(RCore.string.buttonSave),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Preview(name = "EditorTopBar")
@Composable
private fun EditorTopBarPreview() {
    MaterialTheme {
        Surface {
            EditorTopBar(
                onCancel = {},
                onSave = {},
            )
        }
    }
}
