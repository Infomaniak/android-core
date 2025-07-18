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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteUpgradeFeatures
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteUpgradeFeatures.DriveDropboxFeature
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteUpgradeFeatures.DriveStorageFeature
import com.infomaniak.core.myksuite.ui.screens.MyKSuiteUpgradeFeatures.MoreFeatures
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.myksuite.ui.theme.Typography

@Composable
internal fun ColumnScope.UpgradeFeature(
    modifier: Modifier = Modifier,
    customFeature: MyKSuiteUpgradeFeatures,
    iconSize: Dp = Dimens.iconSize,
    textColor: Color = Color.Unspecified,
) {
    val localColors = LocalMyKSuiteColors.current
    Row(
        modifier = modifier
            .align(Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = ImageVector.vectorResource(customFeature.icon),
            contentDescription = null,
            tint = localColors.iconColor,
        )
        Spacer(Modifier.width(Margin.Mini))
        Text(
            modifier = modifier,
            text = stringResource(customFeature.title),
            style = Typography.bodyRegular,
            color = textColor,
        )
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(Margin.Mini)) {
                UpgradeFeature(customFeature = DriveStorageFeature)
                UpgradeFeature(customFeature = DriveDropboxFeature)
                UpgradeFeature(customFeature = MoreFeatures)
            }
        }
    }
}
