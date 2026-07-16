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

import android.R.attr.label
import android.R.attr.onClick
import android.R.attr.type
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.ui.compose.contactcard.R
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun PreviewActionsBottomSheet(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    fun dismissThen(action: () -> Unit) {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(bottom = Margin.Medium)) {
            BottomSheetAction(
                icon = ImageVector.vectorResource(R.drawable.ic_pencil),
                label = stringResource(R.string.contactCardEdit),
                onClick = { dismissThen(onEdit) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Margin.Medium),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            BottomSheetAction(
                icon = ImageVector.vectorResource(R.drawable.ic_bin),
                label = stringResource(R.string.contactCardDelete),
                onClick = { showDeleteConfirmation = true },
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    dismissThen(onDelete)
                }) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            title = { Text(text = stringResource(R.string.contactCardDelete)) },
            text = { Text(text = stringResource(R.string.contactCardDeleteConfirmation)) },
        )
    }
}

@Preview(name = "PreviewActionsBottomSheet")
@Composable
private fun PreviewActionsBottomSheetPreview() {
    MaterialTheme {
        Surface {
            PreviewActionsBottomSheet(
                onDismiss = {},
                onEdit = {},
                onDelete = {},
            )
        }
    }
}
