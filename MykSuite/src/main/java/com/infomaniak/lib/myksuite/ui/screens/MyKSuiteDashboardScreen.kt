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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.lib.myksuite.R
import com.infomaniak.lib.myksuite.ui.components.MyKSuiteChip
import com.infomaniak.lib.myksuite.ui.components.WeightOneSpacer
import com.infomaniak.lib.myksuite.ui.theme.Dimens
import com.infomaniak.lib.myksuite.ui.theme.Margin
import com.infomaniak.lib.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun MyKSuiteDashboardScreen(
    userName: String,
    avatarUri: String = "",
    onClose: () -> Unit = {},
) {
    val paddedModifier = Modifier.padding(horizontal = Margin.Medium)

    Image(
        modifier = Modifier.fillMaxWidth(),
        imageVector = ImageVector.vectorResource(R.drawable.illu_dashboard_background),
        contentDescription = null,
    )
    Column {
        TopAppBar(onClose)
        Card(
            modifier = paddedModifier.padding(top = Margin.Medium),
            shape = RoundedCornerShape(Dimens.largeCornerRadius),
            colors = CardDefaults.cardColors(),
            elevation = CardDefaults.elevatedCardElevation(),
            border = if (isSystemInDarkTheme()) BorderStroke(1.dp, MyKSuiteTheme.colors.cardBorderColor) else null,
        ) {
            Row(modifier = paddedModifier, verticalAlignment = Alignment.CenterVertically) {
                // TODO avatar
                Text(userName)
                WeightOneSpacer(minWidth = Margin.Mini)
                MyKSuiteChip()
            }
            PaddedDivider(paddedModifier)
            AppStorageQuotas(paddedModifier)
            PaddedDivider(paddedModifier)
        }
    }
}

@Composable
private fun TopAppBar(onClose: () -> Unit) {
    Row(modifier = Modifier.padding(top = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            modifier = Modifier
                .padding(start = Margin.Mini)
                .size(48.dp),
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
                .padding(end = 56.dp)
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
            if (it.ordinal == KSuiteApp.entries.lastIndex) Spacer(Modifier.height(Margin.Medium))
        }
    }
}

@Composable
private fun AppStorageQuota(modifier: Modifier = Modifier, app: KSuiteApp) {
    Column(modifier) {
        Row {
            Text(app.displayName)
            WeightOneSpacer(minWidth = Margin.Medium)
            Text("0.2 Go / 20 Go", style = MyKSuiteTheme.typography.bodySmallRegular, color = MyKSuiteTheme.colors.secondaryTextColor)
        }
        Spacer(Modifier.height(Margin.Mini))
    }
}

private enum class KSuiteApp(val displayName: String) {
    Mail("Mail"), Drive("kDrive")
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface(color = Color.White) {
            MyKSuiteDashboardScreen(userName = "Toto", avatarUri = "")
        }
    }
}

