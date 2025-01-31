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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.theme.Dimens
import com.infomaniak.core.myksuite.ui.theme.Margin
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun ExpendableActionItem(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    expendedView: (@Composable () -> Unit)? = null,
) {

    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .heightIn(40.dp)
                .fillMaxWidth()
                .then(  // TODO add onClickLabel for accessibility
                    if (expendedView == null) Modifier else Modifier.clickable { isExpanded = !isExpanded }
                )
                .padding(horizontal = Margin.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
        ) {
            Icon(
                modifier = Modifier.size(Dimens.smallIconSize),
                imageVector = ImageVector.vectorResource(iconRes),
                contentDescription = null,
                tint = MyKSuiteTheme.colors.iconColor,
            )
            Text(
                modifier = Modifier.weight(1.0f),
                text = stringResource(textRes),
                style = MyKSuiteTheme.typography.bodyRegular,
                color = MyKSuiteTheme.colors.primaryTextColor,
            )
            expendedView?.let {
                val icon = ImageVector.vectorResource(if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down)
                Icon(
                    imageVector = icon,
                    contentDescription = null, // TODO
                    tint = MyKSuiteTheme.colors.iconColor,
                )
            }
        }
        AnimatedVisibility(isExpanded) { expendedView?.invoke() }
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            Column {
                ExpendableActionItem(R.drawable.ic_padlock, R.string.myKSuiteDashboardLimitedFunctionalityLabel)
                ExpendableActionItem(
                    iconRes = R.drawable.ic_padlock,
                    textRes = R.string.myKSuiteDashboardLimitedFunctionalityLabel,
                    expendedView = { Text("") }
                )
            }
        }
    }
}
