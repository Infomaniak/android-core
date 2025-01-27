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

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.lib.myksuite.R
import com.infomaniak.lib.myksuite.ui.screens.components.ButtonType
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
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val paddedModifier = Modifier.padding(horizontal = Margin.Large)
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.illu_banner),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            modifier = paddedModifier,
            text = stringResource(R.string.myKSuiteUpgradeTitle),
            textAlign = TextAlign.Center,
            style = MyKSuiteTheme.typography.h2,
            color = MyKSuiteTheme.colors.primaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))
        Text(
            modifier = paddedModifier,
            text = stringResource(R.string.myKSuiteUpgradedescription),
            style = MyKSuiteTheme.typography.bodyRegular,
            color = MyKSuiteTheme.colors.secondaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))

        customFeatures?.let {
            it().forEach { customFeature ->
                Row(
                    modifier = paddedModifier
                        .padding(vertical = Margin.Mini)
                        .align(Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(customFeature.icon),
                        contentDescription = null,
                        tint = MyKSuiteTheme.colors.iconColor,
                    )
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
            modifier = paddedModifier,
            text = stringResource(R.string.myKSuiteUpgradeDetails),
            style = MyKSuiteTheme.typography.bodyRegular,
            color = MyKSuiteTheme.colors.secondaryTextColor,
        )
        Spacer(Modifier.height(Margin.Huge))
        Button(
            modifier = paddedModifier
                .fillMaxWidth()
                .height(56.dp),
            colors = style.colors().buttonColors(),
            shape = style.shape,
            onClick = onButtonClicked,
        ) {
            Text(stringResource(R.string.buttonClose))
        }
        Spacer(Modifier.height(Margin.Large))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            MyKSuiteUpgradeBottomSheet(
                onDismissRequest = {},
                style = ButtonType.Mail,
                customFeatures = {
                    listOf(
                        MyKSuiteUpgradeFeatures(title = R.string.buttonClose, icon = R.drawable.ic_gift),
                        MyKSuiteUpgradeFeatures(title = R.string.buttonClose, icon = R.drawable.ic_gift),
                    )
                },
                onButtonClicked = {},
            )
        }
    }
}
