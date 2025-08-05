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

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Parcelable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.extensions.openUrl
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.MyKSuiteChip
import com.infomaniak.core.myksuite.ui.components.MyKSuitePrimaryButton
import com.infomaniak.core.myksuite.ui.components.MyKSuiteTier
import com.infomaniak.core.myksuite.ui.components.WeightOneSpacer
import com.infomaniak.core.myksuite.ui.components.myKSuiteGradient
import com.infomaniak.core.myksuite.ui.data.AvatarData
import com.infomaniak.core.myksuite.ui.network.ApiRoutes
import com.infomaniak.core.myksuite.ui.screens.components.ExpandableActionItem
import com.infomaniak.core.myksuite.ui.screens.components.InformationBlock
import com.infomaniak.core.myksuite.ui.screens.components.KSuiteProductsWithQuotas
import com.infomaniak.core.myksuite.ui.screens.components.LimitedFunctionalities
import com.infomaniak.core.myksuite.ui.screens.components.MyKSuiteTextItem
import com.infomaniak.core.myksuite.ui.screens.components.ProductsStorageQuotas
import com.infomaniak.core.myksuite.ui.screens.components.UpgradeFeature
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography
import com.infomaniak.core.utils.FORMAT_DATE_SIMPLE
import com.infomaniak.core.utils.format
import kotlinx.parcelize.Parcelize
import java.util.Date

@Composable
fun MyKSuiteDashboardScreen(dashboardScreenData: () -> MyKSuiteDashboardScreenData, onClose: () -> Unit = {}) {

    Box(modifier = Modifier.background(LocalMyKSuiteColors.current.background)) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f),
            contentScale = ContentScale.FillBounds,
            imageVector = ImageVector.vectorResource(R.drawable.illu_dashboard_background),
            contentDescription = null,
        )

        Scaffold(
            topBar = { TopAppBar(onClose) },
            containerColor = Color.Unspecified,
            modifier = Modifier.safeDrawingPadding()
        ) { paddingValues ->
            Box(
                Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = Margin.Large),
            ) {
                Column(
                    Modifier
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(Margin.Large)
                ) {
                    val paddedModifier = Modifier.padding(horizontal = Margin.Medium)
                    SubscriptionInfoCard(paddedModifier, dashboardScreenData)

                    if (dashboardScreenData().myKSuiteTier == MyKSuiteTier.Free) {
                        // TODO: Add this line when we'll have In-app payments
                        // MyKSuitePlusPromotionCard(paddedModifier) {}
                    } else {
                        AdvantagesCard(paddedModifier)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(onClose: () -> Unit) {
    val localColors = LocalMyKSuiteColors.current

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = localColors.topAppBarBackground,
            titleContentColor = localColors.primaryTextColor,
            navigationIconContentColor = localColors.primaryTextColor,
        ),
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_cross_thick),
                    contentDescription = stringResource(R.string.buttonClose),
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.myKSuiteDashboardTitle),
                style = Typography.h2,
            )
        }
    )
}

@Composable
private fun SubscriptionInfoCard(
    @SuppressLint("ModifierParameter") paddedModifier: Modifier,
    dashboardScreenData: () -> MyKSuiteDashboardScreenData,
) {
    val context = LocalContext.current
    val localColors = LocalMyKSuiteColors.current

    Card(
        modifier = paddedModifier.padding(top = Margin.Medium),
        shape = RoundedCornerShape(Dimens.largeCornerRadius),
        colors = CardDefaults.cardColors(containerColor = localColors.background),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.cardElevation),
        border = cardBorder(),
    ) {
        Row(
            modifier = paddedModifier.padding(top = Margin.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
        ) {
            Avatar(
                modifier = Modifier.size(Dimens.avatarSize),
                avatarType = dashboardScreenData().avatarData.toAvatarType(),
                border = myKSuiteGradient(),
            )
            Text(
                modifier = Modifier.weight(1.0f),
                style = Typography.bodyRegular,
                color = localColors.primaryTextColor,
                text = dashboardScreenData().email,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MyKSuiteChip(tier = dashboardScreenData().myKSuiteTier)
        }
        PaddedDivider(paddedModifier)
        ProductsStorageQuotas(
            modifier = paddedModifier,
            myKSuiteTier = dashboardScreenData().myKSuiteTier,
            kSuiteProductsWithQuotas = { dashboardScreenData().kSuiteProductsWithQuotas },
        )
        PaddedDivider(paddedModifier)

        if (dashboardScreenData().myKSuiteTier == MyKSuiteTier.Free) {
            ExpandableActionItem(iconRes = R.drawable.ic_envelope_bold, textRes = R.string.myKSuiteDashboardFreeMailLabel)
            ExpandableActionItem(
                iconRes = R.drawable.ic_padlock,
                textRes = R.string.myKSuiteDashboardLimitedFunctionalityLabel,
                expandedView = {
                    LimitedFunctionalities(
                        modifier = paddedModifier,
                        dailySendingLimit = { dashboardScreenData().dailySendingLimit },
                    )
                },
            )
        } else {
            dashboardScreenData().trialExpiryDate?.let { expiryDate ->
                MyKSuiteTextItem(
                    modifier = paddedModifier.heightIn(min = Dimens.textItemMinHeight),
                    title = stringResource(R.string.myKSuiteDashboardTrialPeriod),
                    value = stringResource(R.string.myKSuiteDashboardUntil, expiryDate.format(FORMAT_DATE_SIMPLE)),
                )
            }
            Spacer(Modifier.height(Margin.Large))
            InformationBlock(
                modifier = paddedModifier,
                text = stringResource(R.string.myKSuiteManageSubscriptionDescription),
                buttonText = stringResource(R.string.myKSuiteManageSubscriptionButton),
                onClick = { context.openUrl(ApiRoutes.MANAGER_URL) },
            )
        }
        Spacer(Modifier.height(Margin.Medium))
    }
}

@Composable
private fun PaddedDivider(modifier: Modifier) {
    Spacer(Modifier.height(Margin.Large))
    HorizontalDivider(modifier)
    Spacer(Modifier.height(Margin.Large))
}

// TODO: Add this when we'll have in-app payments
@Composable
private fun MyKSuitePlusPromotionCard(modifier: Modifier = Modifier, onButtonClicked: () -> Unit) {
    val localColors = LocalMyKSuiteColors.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.largeCornerRadius),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.cardElevation),
        border = myKSuiteGradient(),
    ) {
        Box(Modifier.padding(Margin.Medium)) {
            Column(verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        modifier = Modifier.width(88.dp),
                        contentScale = ContentScale.FillWidth,
                        imageVector = ImageVector.vectorResource(R.drawable.ic_logo_my_ksuite_plus),
                        contentDescription = stringResource(R.string.myKSuitePlusName),
                    )
                    WeightOneSpacer(minWidth = Margin.Large)
                    Text(
                        text = stringResource(R.string.myKSuiteDashboardFreeTrialTitle),
                        style = Typography.labelMedium,
                        color = localColors.primaryTextColor,
                        modifier = Modifier
                            .background(
                                color = localColors.secondaryBackground,
                                shape = RoundedCornerShape(Dimens.largeCornerRadius),
                            )
                            .padding(horizontal = Margin.Mini, vertical = Margin.Micro),
                    )
                }
                Text(
                    text = stringResource(R.string.myKSuiteDashboardFreeTrialDescription),
                    style = Typography.bodySmallRegular,
                    color = localColors.primaryTextColor,
                )
                MyKSuitePrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.myKSuiteDashboardFreeTrialButton),
                    shape = RoundedCornerShape(Dimens.largeCornerRadius),
                    onClick = onButtonClicked,
                )
            }
        }
    }
}

@Composable
private fun AdvantagesCard(modifier: Modifier) {
    val localColors = LocalMyKSuiteColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.largeCornerRadius),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.cardElevation),
        colors = CardDefaults.elevatedCardColors(containerColor = localColors.background),
        border = cardBorder(),
    ) {
        Column(modifier = Modifier.padding(Margin.Medium), verticalArrangement = Arrangement.spacedBy(Margin.Large)) {
            Text(
                text = stringResource(R.string.myKSuiteUpgradeBenefitsTitle),
                color = localColors.secondaryTextColor,
                style = Typography.bodySmallRegular,
            )

            val upgradeFeatureModifier = Modifier.fillMaxWidth()
            val iconSize = Dimens.smallIconSize
            UpgradeFeature(upgradeFeatureModifier, MyKSuiteUpgradeFeatures.DriveStorageFeature, iconSize)
            UpgradeFeature(upgradeFeatureModifier, MyKSuiteUpgradeFeatures.MailUnlimitedFeature, iconSize)
            UpgradeFeature(upgradeFeatureModifier, MyKSuiteUpgradeFeatures.MoreFeatures, iconSize)
        }
    }
}

@Composable
private fun cardBorder() = if (isSystemInDarkTheme()) BorderStroke(1.dp, LocalMyKSuiteColors.current.cardBorderColor) else null

@Parcelize
data class MyKSuiteDashboardScreenData(
    val myKSuiteTier: MyKSuiteTier,
    val email: String,
    val dailySendingLimit: String,
    val kSuiteProductsWithQuotas: List<KSuiteProductsWithQuotas>,
    val trialExpiryDate: Date?,
    val avatarData: AvatarData,
) : Parcelable

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    val dashboardScreenData = MyKSuiteDashboardScreenData(
        myKSuiteTier = MyKSuiteTier.Plus,
        email = "Tata",
        avatarData = AvatarData(
            id = "DOUZE",
            uri = "",
            userInitials = "IK",
            initialsColor = Color.White.toArgb(),
            backgroundColor = Color.DarkGray.toArgb(),
        ),
        dailySendingLimit = "500",
        kSuiteProductsWithQuotas = listOf(
            KSuiteProductsWithQuotas.Mail(usedSize = "0.2 Go", maxSize = "20 Go", progress = 0.01f),
            KSuiteProductsWithQuotas.Drive(usedSize = "6 Go", maxSize = "15 Go", progress = 0.4f),
        ),
        trialExpiryDate = Date(),
    )

    MyKSuiteTheme {
        Surface(Modifier.fillMaxSize(), color = Color.White) {
            MyKSuiteDashboardScreen(dashboardScreenData = { dashboardScreenData })
        }
    }
}
