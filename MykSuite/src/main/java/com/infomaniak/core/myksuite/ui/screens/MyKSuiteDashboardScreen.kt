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
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.components.*
import com.infomaniak.core.myksuite.ui.screens.components.*
import com.infomaniak.core.myksuite.ui.theme.*
import com.infomaniak.core.myksuite.ui.theme.Typography

@Composable
fun MyKSuiteDashboardScreen(
    userName: String,
    avatarUri: String = "",
    dailySendingLimit: String,
    onClose: () -> Unit = {},
) {
    MyKSuiteTheme {
        Scaffold(
            topBar = { TopAppBar(onClose) },
            containerColor = LocalMyKSuiteColors.current.onPrimaryButton,
        ) { paddingValues ->
            Box(
                Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.45f),
                    contentScale = ContentScale.FillBounds,
                    imageVector = ImageVector.vectorResource(R.drawable.illu_dashboard_background),
                    contentDescription = null,
                )
                Column(Modifier.padding(paddingValues), verticalArrangement = Arrangement.spacedBy(Margin.Large)) {
                    val paddedModifier = Modifier.padding(horizontal = Margin.Medium)
                    SubscriptionInfoCard(paddedModifier, avatarUri, userName, dailySendingLimit)
                    // TODO: Add this line when we'll have In-app payments
                    // MyKSuitePlusPromotionCard(paddedModifier) {}
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
    paddedModifier: Modifier,
    avatarUri: String,
    userName: String,
    dailySendingLimit: String,
) {
    val localColors = LocalMyKSuiteColors.current

    Card(
        modifier = paddedModifier.padding(top = Margin.Medium),
        shape = RoundedCornerShape(Dimens.largeCornerRadius),
        colors = CardDefaults.cardColors(containerColor = localColors.background),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.cardElevation),
        border = if (isSystemInDarkTheme()) BorderStroke(1.dp, localColors.cardBorderColor) else null,
    ) {
        Row(
            modifier = paddedModifier.padding(top = Margin.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
        ) {
            UserAvatar(avatarUri)
            Text(
                modifier = Modifier.weight(1.0f),
                style = Typography.bodyRegular,
                color = localColors.primaryTextColor,
                text = userName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MyKSuiteChip(tier = MyKSuiteTier.Free)
        }
        PaddedDivider(paddedModifier)
        AppStorageQuotas(paddedModifier)
        PaddedDivider(paddedModifier)
        ExpendableActionItem(iconRes = R.drawable.ic_envelope, textRes = R.string.myKSuiteDashboardFreeMailLabel)
        ExpendableActionItem(
            iconRes = R.drawable.ic_padlock,
            textRes = R.string.myKSuiteDashboardLimitedFunctionalityLabel,
            expendedView = { LimitedFunctionalities(paddedModifier, dailySendingLimit) },
        )
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
                    colors = {
                        MyKSuiteButtonColors(
                            containerColor = localColors.primaryButton,
                            contentColor = localColors.onPrimaryButton,
                        )
                    },
                    shape = RoundedCornerShape(Dimens.largeCornerRadius),
                    onClick = onButtonClicked,
                )
            }
        }
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    Surface(Modifier.fillMaxSize(), color = Color.White) {
        MyKSuiteDashboardScreen(userName = "Toto", avatarUri = "", dailySendingLimit = "500")
    }
}
