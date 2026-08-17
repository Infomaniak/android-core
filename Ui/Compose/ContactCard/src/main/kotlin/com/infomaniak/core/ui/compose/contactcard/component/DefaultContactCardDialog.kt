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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.infomaniak.core.ui.compose.contactcard.R
import com.infomaniak.core.common.R as RCore

@Composable
internal fun DefaultContactCardDialog(
    titleRes: Int,
    descriptionRes: Int,
    confirmButtonRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissButtonRes: Int? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(confirmButtonRes))
            }
        },
        dismissButton = dismissButtonRes?.let {
            {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(it))
                }
            }
        },
        title = { Text(text = stringResource(titleRes)) },
        text = { Text(text = stringResource(descriptionRes)) },
    )
}

@Composable
internal fun DefaultValidationErrorDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DefaultContactCardDialog(
        titleRes = R.string.alertTitle,
        descriptionRes = R.string.alertDescription,
        confirmButtonRes = android.R.string.ok,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun DefaultDeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DefaultContactCardDialog(
        titleRes = R.string.deleteAlertTitle,
        descriptionRes = R.string.deleteAlertDescription,
        confirmButtonRes = R.string.deleteButton,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        dismissButtonRes = RCore.string.buttonCancel,
    )
}
