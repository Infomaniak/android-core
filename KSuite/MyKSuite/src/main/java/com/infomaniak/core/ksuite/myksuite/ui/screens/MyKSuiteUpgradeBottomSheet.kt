/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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
import android.os.Build.VERSION
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogWindowProvider
import com.infomaniak.core.ksuite.myksuite.R
import com.infomaniak.core.ksuite.myksuite.ui.components.MyKSuitePrimaryButton
import com.infomaniak.core.ksuite.myksuite.ui.screens.components.UpgradeFeature
import com.infomaniak.core.ksuite.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.ksuite.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.ui.compose.basics.ButtonType
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.serialization.Serializable
import com.infomaniak.core.common.R as RCore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyKSuiteUpgradeBottomSheet(
    modifier: Modifier = Modifier,
    app: KSuiteApp,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest, modifier, sheetState, scrimColor = Color.Transparent) {
        (LocalView.current.parent as? DialogWindowProvider)?.window?.let { window ->
            LaunchedEffect(Unit) {
                if (VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
            }
        }

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
            imageVector = ImageVector.vectorResource(app.bannerRes),
            contentScale = if (app.isBannerStyle) ContentScale.FillWidth else ContentScale.None,
            contentDescription = null,
            modifier = if (app.isBannerStyle) Modifier.fillMaxWidth() else Modifier.padding(bottom = Margin.Huge),
        )
        Text(
            modifier = paddedModifier,
            text = stringResource(app.titleRes),
            textAlign = TextAlign.Center,
            style = Typography.h2,
            color = localColors.primaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))
        Text(
            modifier = paddedModifier,
            text = stringResource(app.descriptionRes),
            style = Typography.bodyRegular,
            color = localColors.secondaryTextColor,
        )
        Spacer(Modifier.height(Margin.Medium))
        UpgradeFeatures(app, paddedModifier)
        Spacer(Modifier.height(Margin.Large))
        Text(
            modifier = paddedModifier,
            text = stringResource(app.detailsRes),
            style = Typography.bodyRegular,
            color = localColors.secondaryTextColor,
        )
        Spacer(Modifier.height(Margin.Huge))
        MyKSuitePrimaryButton(
            modifier = paddedModifier.fillMaxWidth(),
            text = stringResource(RCore.string.buttonClose),
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

@Serializable
sealed class KSuiteApp(
    internal val features: List<MyKSuiteUpgradeFeatures>,
    internal val buttonStyle: ButtonType,
) {
    // Cannot use 'open' val because of https://youtrack.jetbrains.com/issue/KT-38958
    abstract val titleRes: Int
    abstract val descriptionRes: Int
    abstract val detailsRes: Int
    abstract val bannerRes: Int
    abstract val isBannerStyle: Boolean

    @Serializable
    data class Mail(
        override val titleRes: Int = R.string.myKSuiteUpgradeTitle,
        override val descriptionRes: Int = R.string.myKSuiteUpgradeDescription,
        override val detailsRes: Int = R.string.myKSuiteUpgradeDetails,
        override val bannerRes: Int = R.drawable.illu_banner,
        override val isBannerStyle: Boolean = true
    ) : KSuiteApp(
        features = listOf(MyKSuiteUpgradeFeatures.MailUnlimitedFeature, MyKSuiteUpgradeFeatures.MailOtherFeature),
        buttonStyle = ButtonType.Mail,
    )

    @Serializable
    data object Drive : KSuiteApp(
        features = listOf(MyKSuiteUpgradeFeatures.DriveStorageFeature, MyKSuiteUpgradeFeatures.DriveDropboxFeature),
        buttonStyle = ButtonType.Drive,
    ) {
        override val titleRes = R.string.myKSuiteUpgradeTitle
        override val descriptionRes = R.string.myKSuiteUpgradeDescription
        override val detailsRes = R.string.myKSuiteUpgradeDetails
        override val bannerRes = R.drawable.illu_banner
        override val isBannerStyle = true
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            UpgradeBottomSheetContent(app = KSuiteApp.Mail(), onButtonClicked = {})
        }
    }
}
