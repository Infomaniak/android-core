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
package com.infomaniak.core.ksuite.myksuite.ui.screens

import android.content.res.Configuration
import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.compose.basics.ButtonType
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.ksuite.myksuite.R
import com.infomaniak.core.ksuite.myksuite.ui.components.MyKSuitePrimaryButton
import com.infomaniak.core.ksuite.myksuite.ui.screens.components.UpgradeFeature
import com.infomaniak.core.ksuite.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.ksuite.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.ksuite.myksuite.ui.theme.Typography
import kotlinx.parcelize.Parcelize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyKSuiteUpgradeBottomSheet(
    modifier: Modifier = Modifier,
    app: KSuiteApp,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest, modifier, sheetState) {
        UpgradeBottomSheetContent(app, onDismissRequest)
    }
}

@Composable
private fun UpgradeBottomSheetContent(app: KSuiteApp, onButtonClicked: () -> Unit) {
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
        UpgradeFeatures(app, paddedModifier)
        Spacer(Modifier.height(Margin.Large))
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
            shape = app.buttonStyle.shape,
            onClick = onButtonClicked,
        )
        Spacer(Modifier.height(Margin.Large))
    }
}

@Composable
private fun UpgradeFeatures(app: KSuiteApp, modifier: Modifier) {
    val upgradeFeatureModifier = Modifier.fillMaxWidth()
    val textColor = LocalMyKSuiteColors.current.secondaryTextColor

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
        app.features.forEach { UpgradeFeature(modifier = upgradeFeatureModifier, customFeature = it, textColor = textColor) }
        UpgradeFeature(
            modifier = upgradeFeatureModifier,
            customFeature = MyKSuiteUpgradeFeatures.MoreFeatures,
            textColor = textColor,
        )
    }
}

@Parcelize
enum class KSuiteApp(
    internal val features: List<MyKSuiteUpgradeFeatures>,
    internal val buttonStyle: ButtonType,
) : Parcelable {
    Mail(
        features = listOf(MyKSuiteUpgradeFeatures.MailUnlimitedFeature, MyKSuiteUpgradeFeatures.MailOtherFeature),
        buttonStyle = ButtonType.Mail,
    ),
    Drive(
        features = listOf(MyKSuiteUpgradeFeatures.DriveStorageFeature, MyKSuiteUpgradeFeatures.DriveDropboxFeature),
        buttonStyle = ButtonType.Drive,
    ),
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            UpgradeBottomSheetContent(app = KSuiteApp.Mail, onButtonClicked = {})
        }
    }
}
