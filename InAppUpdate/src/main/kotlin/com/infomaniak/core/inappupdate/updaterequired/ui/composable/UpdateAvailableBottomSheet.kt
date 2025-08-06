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
package com.infomaniak.core.inappupdate.updaterequired.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.infomaniak.core.inappupdate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateAvailableBottomSheet(
    illustration: Painter,
    titleTextStyle: TextStyle,
    descriptionTextStyle: TextStyle,
    installUpdateButton: @Composable() () -> Unit,
    dismissButton: @Composable() () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = SheetState(skipPartiallyExpanded = true, density = Density(LocalContext.current)),
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(painter = illustration, contentDescription = null)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                style = titleTextStyle,
                text = stringResource(R.string.updateAvailableTitle),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                style = descriptionTextStyle,
                textAlign = TextAlign.Center,
                text = stringResource(R.string.updateAvailableDescription)
            )

            Spacer(modifier = Modifier.height(32.dp))

            installUpdateButton()

            dismissButton()
        }
    }
}
