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
package com.infomaniak.lib.myksuite.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.infomaniak.lib.myksuite.ui.theme.Margin
import com.infomaniak.lib.myksuite.ui.theme.MyKSuiteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyKSuiteUpgradeBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismissRequest: () -> Unit,
    customFeatures: @Composable (() -> List<MyKSuiteUpgradeFeatures>)?,
    style: ButtonType,
    onButtonClicked: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest, modifier, sheetState) {
        BottomSheetContent(customFeatures, style, onButtonClicked)
    }
}

@Composable
private fun BottomSheetContent(
    customFeatures: @Composable (() -> List<MyKSuiteUpgradeFeatures>)?,
    style: ButtonType,
    onButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = Margin.Large)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // Image(modifier = paddedModifier, imageVector = ImageVector.vectorResource(R.), contentDescription = null) // TODO add banner
        Spacer(Modifier.height(Margin.Huge))
        Text(
            text = "Donner plus d’espace à vos idées",
            textAlign = TextAlign.Center,
            style = MyKSuiteTheme.typography.bodyMedium,
            color = MyKSuiteTheme.colors.primaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))
        Text(
            text = "Plus d’espace, de possibilités et de flexibilité pour faire ce que vous aimez.",
            style = MyKSuiteTheme.typography.bodyRegular,
            color = MyKSuiteTheme.colors.secondaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))

        customFeatures?.let {
            it().forEach { customFeature ->
                Row(Modifier.padding(vertical = Margin.Mini)) {
                    Icon(ImageVector.vectorResource(customFeature.icon), contentDescription = null)
                    Spacer(Modifier.width(Margin.Mini))
                    Text(
                        text = stringResource(customFeature.title),
                        style = MyKSuiteTheme.typography.bodyRegular,
                        color = MyKSuiteTheme.colors.secondaryTextColor,
                    )
                }
            }
            Spacer(Modifier.height(Margin.Large))
        }

        Text(
            text = "Pour bénéficier de my kSuite+, modifiez votre offre depuis votre interface web.",
            style = MyKSuiteTheme.typography.bodyRegular,
            color = MyKSuiteTheme.colors.secondaryTextColor,
        )
        Spacer(Modifier.height(Margin.Huge))
        Button(
            colors = style.colors().buttonColors(),
            shape = style.shape,
            onClick = onButtonClicked,
        ) {
            Text("Fermer") // TODO
        }
    }
}

enum class ButtonType(val colors: @Composable () -> MyKSuiteButtonColors, val shape: Shape) {
    Mail(
        colors = { MyKSuiteButtonColors(containerColor = MyKSuiteTheme.colors.mailButton) },
        shape = RoundedCornerShape(16.dp),
    ),
    Drive(
        colors = { MyKSuiteButtonColors(containerColor = MyKSuiteTheme.colors.driveButton) },
        shape = RoundedCornerShape(8.dp),
    ),
}

data class MyKSuiteButtonColors(
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val disabledContainerColor: Color = Color.Unspecified,
    val disabledContentColor: Color = Color.Unspecified,
) {
    @Composable
    fun buttonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )
}

data class MyKSuiteUpgradeFeatures(@StringRes val title: Int, @DrawableRes val icon: Int)
