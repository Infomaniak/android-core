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
package com.infomaniak.core.myksuite.ui.screens

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
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.MyKSuitePrimaryButton
import com.infomaniak.core.myksuite.ui.screens.components.MyKSuiteButtonType
import com.infomaniak.core.myksuite.ui.screens.components.UpgradeFeature
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.Margin
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyKSuiteUpgradeBottomSheet(
    modifier: Modifier = Modifier,
    style: MyKSuiteButtonType,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismissRequest: () -> Unit,
    customFeatures: (() -> List<MyKSuiteUpgradeFeatures>)?,
) {
    MyKSuiteTheme {
        ModalBottomSheet(onDismissRequest, modifier, sheetState) {
            UpgradeBottomSheetContent(customFeatures, style, onDismissRequest)
        }
    }
}

@Composable
private fun UpgradeBottomSheetContent(
    customFeatures: (() -> List<MyKSuiteUpgradeFeatures>)?,
    style: MyKSuiteButtonType,
    onButtonClicked: () -> Unit,
) {
    val localColors = LocalMyKSuiteColors.current

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
            style = Typography.h2,
            color = localColors.primaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))
        Text(
            modifier = paddedModifier,
            text = stringResource(R.string.myKSuiteUpgradeDescription),
            style = Typography.bodyRegular,
            color = localColors.secondaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))
        UpgradeFeatures(customFeatures, paddedModifier)
        Text(
            modifier = paddedModifier,
            text = stringResource(R.string.myKSuiteUpgradeDetails),
            style = Typography.bodyRegular,
            color = localColors.secondaryTextColor,
        )
        Spacer(Modifier.height(Margin.Huge))
        MyKSuitePrimaryButton(
            modifier = paddedModifier.fillMaxWidth(),
            text = stringResource(R.string.buttonClose),
            colors = style.colors,
            shape = style.shape,
            onClick = onButtonClicked,
        )
        Spacer(Modifier.height(Margin.Large))
    }
}

@Composable
private fun ColumnScope.UpgradeFeatures(
    customFeatures: (() -> List<MyKSuiteUpgradeFeatures>)?,
    modifier: Modifier,
) {
    customFeatures?.let {
        it().forEach { UpgradeFeature(it, modifier) }
        UpgradeFeature(MyKSuiteUpgradeFeatures.MoreFeatures, modifier)
        Spacer(Modifier.height(Margin.Large))
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            UpgradeBottomSheetContent(
                customFeatures = {
                    listOf(MyKSuiteUpgradeFeatures(title = R.string.myKSuiteUpgradeLabel, icon = R.drawable.ic_gift))
                },
                style = MyKSuiteButtonType.Mail,
                onButtonClicked = {}
            )
        }
    }
}
