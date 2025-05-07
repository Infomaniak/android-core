/*
 * Infomaniak SwissTransfer - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
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
package com.infomaniak.core.inappstore.ui.screen

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.inappstore.R
import com.infomaniak.core.ui.theme.Margin

@Composable
fun UpdateRequired(onBack: () -> Unit, onInstallButtonClicked: () -> Unit, @DrawableRes appIllustration: Int) {
    BackHandler { onBack() }

    LocalContext.current

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
    ) { padding ->
        Column {
            Box(modifier = Modifier.weight(1.0f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val paddedModifier = Modifier.padding(horizontal = Margin.Medium)

                    Image(
                        modifier = paddedModifier,
                        painter = painterResource(appIllustration),
                        contentDescription = null,
                    )

                    Spacer(Modifier.height(Margin.Large))

                    Text(
                        text = stringResource(R.string.updateAppTitle),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = paddedModifier,
                    )

                    Spacer(Modifier.height(Margin.Large))

                    Text(
                        text = stringResource(R.string.updateRequiredDescription),
                        textAlign = TextAlign.Center,
                        // style = InAppStoreTheme.typography.bodyRegular,
                        // color = InAppStoreTheme.colors.secondaryTextColor,
                        modifier = paddedModifier,
                    )
                }
            }

            // TODO: Style the update button (use the app theme too).
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Margin.Small),
                onClick = onInstallButtonClicked,
            ) {
                Text(stringResource(R.string.updateInstallButton))
            }
        }
    }
}

// private fun MaterialButton.getPrimaryColor() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//     MaterialColors.getColor(ContextThemeWrapper(context, theme), RMaterial.attr.colorPrimary, UNDEFINED_PRIMARY_COLOR)
// } else {
//     UNDEFINED_PRIMARY_COLOR
// }

@Preview
@Composable
private fun Preview() {
    UpdateRequired(onBack = {}, onInstallButtonClicked = {}, appIllustration = R.drawable.illu_update_required_example)
}
