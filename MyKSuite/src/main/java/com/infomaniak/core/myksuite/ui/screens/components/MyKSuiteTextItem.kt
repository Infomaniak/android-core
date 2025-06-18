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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography
import com.infomaniak.core.utils.FORMAT_DATE_SIMPLE
import com.infomaniak.core.utils.format
import java.util.Date

@Composable
internal fun MyKSuiteTextItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueStyle: TextStyle = Typography.bodyRegular,
) {
    val localColors = LocalMyKSuiteColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, color = localColors.primaryTextColor)
        Text(text = value, color = localColors.secondaryTextColor, style = valueStyle)
    }
}

@Composable
@Preview
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            MyKSuiteTextItem(
                modifier = Modifier.padding(horizontal = Margin.Medium),
                title = stringResource(R.string.myKSuiteDashboardTrialPeriod),
                value = stringResource(R.string.myKSuiteDashboardUntil, Date().format(FORMAT_DATE_SIMPLE)),
            )
        }
    }
}
