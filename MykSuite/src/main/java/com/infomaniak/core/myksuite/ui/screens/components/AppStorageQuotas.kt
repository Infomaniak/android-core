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
package com.infomaniak.core.myksuite.ui.screens.components

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
import com.infomaniak.core.myksuite.ui.components.WeightOneSpacer
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.Margin
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography

@Composable
internal fun ProductsStorageQuotas(modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Margin.Medium)) {
        KSuiteProductsWithQuotas.entries.forEach { ProductStorageQuota(app = it) }
    }
}

@Composable
private fun ProductStorageQuota(modifier: Modifier = Modifier, app: KSuiteProductsWithQuotas) {
    val localColors = LocalMyKSuiteColors.current
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = app.displayName,
                style = Typography.bodyRegular,
                color = localColors.primaryTextColor,
            )
            WeightOneSpacer(minWidth = Margin.Medium)
            Text(
                text = "0.2 Go / 20 Go", // TODO: Use real data
                style = Typography.bodySmallRegular,
                color = localColors.secondaryTextColor,
            )
        }
        Spacer(Modifier.height(Margin.Mini))
        val progressIndicatorHeight = 14.dp
        LinearProgressIndicator(
            modifier = Modifier
                .height(progressIndicatorHeight)
                .fillMaxWidth(),
            color = app.color(),
            trackColor = localColors.chipBackground,
            strokeCap = StrokeCap.Round,
            gapSize = -progressIndicatorHeight,
            progress = { 0.5f }, // TODO: Use real values
            drawStopIndicator = {},
        )
    }
}

private enum class KSuiteProductsWithQuotas(val displayName: String, val color: @Composable () -> Color) {
    Mail("Mail", { LocalMyKSuiteColors.current.mail }),
    Drive("kDrive", { LocalMyKSuiteColors.current.drive }),
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            ProductsStorageQuotas(Modifier.padding(Margin.Medium))
        }
    }
}
