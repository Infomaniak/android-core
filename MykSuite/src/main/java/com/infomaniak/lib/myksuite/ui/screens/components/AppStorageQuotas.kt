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
package com.infomaniak.lib.myksuite.ui.screens.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.lib.myksuite.ui.components.WeightOneSpacer
import com.infomaniak.lib.myksuite.ui.theme.Margin
import com.infomaniak.lib.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun AppStorageQuotas(modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
        KSuiteApp.entries.forEach { AppStorageQuota(app = it) }
    }
}

@Composable
private fun AppStorageQuota(modifier: Modifier = Modifier, app: KSuiteApp) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = app.displayName,
                style = MyKSuiteTheme.typography.bodyRegular,
                color = MyKSuiteTheme.colors.primaryTextColor,
            )
            WeightOneSpacer(minWidth = Margin.Medium)
            Text(
                text = "0.2 Go / 20 Go", // TODO: Use real data
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
        Surface {
            AppStorageQuotas(Modifier.padding(Margin.Medium))
        }
    }
}
