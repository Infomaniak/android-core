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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.infomaniak.lib.myksuite.R
import com.infomaniak.lib.myksuite.ui.components.MyKSuiteChip
import com.infomaniak.lib.myksuite.ui.components.WeightOneSpacer
import com.infomaniak.lib.myksuite.ui.screens.components.ExpendableActionItem
import com.infomaniak.lib.myksuite.ui.theme.Dimens
import com.infomaniak.lib.myksuite.ui.theme.Margin
import com.infomaniak.lib.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun MyKSuiteDashboardScreen(
    userName: String,
    avatarUri: String = "",
    dailySendingLimit: () -> String,
    onClose: () -> Unit = {},
) {
    val paddedModifier = Modifier.padding(horizontal = Margin.Medium)

    Box(Modifier.verticalScroll(rememberScrollState())) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f),
            contentScale = ContentScale.FillBounds,
            imageVector = ImageVector.vectorResource(R.drawable.illu_dashboard_background),
            contentDescription = null,
        )
        Column(Modifier.fillMaxSize()) {
            TopAppBar(onClose)
            Card(
                modifier = paddedModifier.padding(top = Margin.Medium),
                shape = RoundedCornerShape(Dimens.largeCornerRadius),
                colors = CardDefaults.cardColors(),
                elevation = CardDefaults.elevatedCardElevation(),
                border = if (isSystemInDarkTheme()) BorderStroke(1.dp, MyKSuiteTheme.colors.cardBorderColor) else null,
            ) {
                Row(
                    modifier = paddedModifier.padding(top = Margin.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
                ) {
                    UserAvatar(avatarUri)
                    Text(modifier = Modifier.weight(1.0f), text = userName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    MyKSuiteChip()
                }
                PaddedDivider(paddedModifier)
                AppStorageQuotas(paddedModifier)
                PaddedDivider(paddedModifier)
            }
        }
    }
}

@Composable
private fun UserAvatar(avatarUri: String) {
    val context = LocalContext.current
    var shouldDisplayPreview by rememberSaveable(avatarUri) { mutableStateOf(true) }

    Box {
        if (shouldDisplayPreview) {
            val imageRequest = remember(avatarUri) {
                ImageRequest.Builder(context)
                    .data(avatarUri)
                    .crossfade(true)
                    .build()
            }

            AsyncImage(
                modifier = Modifier.size(Dimens.iconSize),
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { shouldDisplayPreview = false },
            )
        } else {
            Image(
                modifier = Modifier
                    .size(Dimens.iconSize)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            0.0f to Color(0xFF1DDDFD),
                            0.1f to Color(0xFF337CFF),
                            0.3f to Color(0xFFA055FC),
                            0.4f to Color(0xFFF34BBB),
                            0.6f to Color(0xFFFD8C3D),
                            start = Offset(0.0f, 10.0f),
                            end = Offset(0.0f, 90.0f),
                        ),
                        shape = RoundedCornerShape(Dimens.largeCornerRadius),
                    ),
                imageVector = ImageVector.vectorResource(R.drawable.ic_person),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun TopAppBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier.padding(top = 56.dp, bottom = Margin.Medium, start = Margin.Mini, end = Margin.Mini),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            modifier = Modifier.size(Dimens.iconButtonSize),
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MyKSuiteTheme.colors.iconColor),
        ) {
            Icon(
                modifier = Modifier.size(Dimens.iconSize),
                imageVector = ImageVector.vectorResource(R.drawable.ic_cross_thick),
                contentDescription = stringResource(R.string.buttonClose),
            )
        }
        Text(
            modifier = Modifier
                .padding(end = Dimens.iconButtonSize)
                .fillMaxWidth(),
            text = "Mon abonnement",
            style = MyKSuiteTheme.typography.h2,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PaddedDivider(modifier: Modifier) {
    Spacer(Modifier.height(Margin.Large))
    HorizontalDivider(modifier)
    Spacer(Modifier.height(Margin.Large))
}

@Composable
private fun AppStorageQuotas(modifier: Modifier) {
    Column(modifier = modifier) {
        KSuiteApp.entries.forEach {
            AppStorageQuota(app = it)
            if (it.ordinal != KSuiteApp.entries.lastIndex) Spacer(Modifier.height(Margin.Medium))
        }
    }
}

@Composable
private fun AppStorageQuota(modifier: Modifier = Modifier, app: KSuiteApp) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(app.displayName)
            WeightOneSpacer(minWidth = Margin.Medium)
            Text(
                "0.2 Go / 20 Go", // TODO: Use real data
                style = MyKSuiteTheme.typography.bodySmallRegular,
                color = MyKSuiteTheme.colors.secondaryTextColor,
            )
        }
        Spacer(Modifier.height(Margin.Mini))
        LinearProgressIndicator(
            modifier = Modifier
                .height(14.dp)
                .fillMaxWidth(),
            color = app.color(),
            trackColor = MyKSuiteTheme.colors.chipBackground,
            strokeCap = StrokeCap.Round,
            gapSize = -Margin.Mini,
            progress = { 0.5f }, // TODO: Use real values
            drawStopIndicator = {},
        )
    }
}

private enum class KSuiteApp(val displayName: String, val color: @Composable () -> Color) {
    Mail("Mail", { MyKSuiteTheme.colors.mail }), Drive("kDrive", { MyKSuiteTheme.colors.drive })
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface(Modifier.fillMaxSize(), color = Color.White) {
            MyKSuiteDashboardScreen(userName = "Toto", avatarUri = "", dailySendingLimit = { "500" })
        }
    }
}

