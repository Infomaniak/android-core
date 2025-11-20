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
package com.infomaniak.core.ksuite.myksuite.ui.screens.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ksuite.myksuite.R
import com.infomaniak.core.ksuite.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.ksuite.myksuite.ui.theme.MyKSuiteTheme

@Composable
internal fun LimitedFunctionalities(modifier: Modifier, dailySendingLimit: () -> String) {
    Column(
        modifier = modifier.padding(top = Margin.Mini),
        verticalArrangement = Arrangement.spacedBy(Margin.Mini),
    ) {
        LimitedFunctionalityLabel(textRes = R.string.myKSuiteDashboardFunctionalityMailAndDrive)
        Row(verticalAlignment = Alignment.CenterVertically) {
            LimitedFunctionalityLabel(modifier = Modifier.weight(1.0f), R.string.myKSuiteDashboardFunctionalityLimit)
            Text(
                modifier = Modifier.padding(start = Margin.Mini),
                text = dailySendingLimit(),
                style = Typography.bodySmallMedium,
            )
        }
        LimitedFunctionalityLabel(textRes = R.string.myKSuiteDashboardFunctionalityCustomReminders)
    }
}

@Composable
private fun LimitedFunctionalityLabel(modifier: Modifier = Modifier, @StringRes textRes: Int) {
    Text(
        modifier = modifier.fillMaxWidth(0.9f),
        text = stringResource(textRes),
        style = Typography.bodySmallRegular,
        color = LocalMyKSuiteColors.current.secondaryTextColor,
    )
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            LimitedFunctionalities(Modifier.padding(horizontal = Margin.Medium), dailySendingLimit = { "500" })
        }
    }
}
