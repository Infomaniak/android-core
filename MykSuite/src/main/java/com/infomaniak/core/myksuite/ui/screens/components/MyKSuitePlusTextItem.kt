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

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.Margin
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.utils.FORMAT_DATE_SIMPLE
import com.infomaniak.core.utils.format
import java.util.Date

@Composable
internal fun MyKSuitePlusTextItem(modifier: Modifier = Modifier, title: String, value: String) {
    Row(
        modifier = modifier
            .heightIn(min = Dimens.textItemMinHeight)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title)
        Text(text = value, color = LocalMyKSuiteColors.current.secondaryTextColor)
    }
}

@Composable
@Preview
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            MyKSuitePlusTextItem(
                modifier = Modifier.padding(horizontal = Margin.Medium),
                title = stringResource(R.string.myKSuiteDashboardTrialPeriod),
                value = stringResource(R.string.myKSuiteDashboardUntil, Date().format(FORMAT_DATE_SIMPLE)),
            )
        }
    }
}
