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
package com.infomaniak.core.inappupdate.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.infomaniak.core.extensions.appName
import com.infomaniak.core.inappupdate.R
import com.infomaniak.core.ui.compose.bottomstickybuttonscaffolds.DoubleStackedButtonScaffold
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
fun UpdateAvailableBottomSheetContent(
    illustration: Painter,
    titleTextStyle: TextStyle,
    descriptionTextStyle: TextStyle,
    installUpdateButton: @Composable ((Modifier) -> Unit),
    dismissButton: @Composable ((Modifier) -> Unit),
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painter = illustration, contentDescription = null)

        Spacer(modifier = Modifier.height(Margin.Medium))

        Text(
            modifier = Modifier.padding(horizontal = Margin.Medium),
            style = titleTextStyle,
            text = stringResource(R.string.updateAvailableTitle),
        )

        Spacer(modifier = Modifier.height(Margin.Huge))

        Text(
            modifier = Modifier.padding(horizontal = Margin.Medium),
            style = descriptionTextStyle,
            textAlign = TextAlign.Center,
            text = stringResource(R.string.updateAvailableDescription, appName),
        )

        Spacer(modifier = Modifier.height(Margin.Huge))

        DoubleStackedButtonScaffold(
            topButton = installUpdateButton,
            bottomButton = dismissButton,
        )
    }
}
