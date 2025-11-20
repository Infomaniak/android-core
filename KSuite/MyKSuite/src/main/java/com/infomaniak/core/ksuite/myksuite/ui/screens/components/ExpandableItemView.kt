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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.ui.compose.basics.Dimens
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ksuite.myksuite.R
import com.infomaniak.core.ksuite.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.ksuite.myksuite.ui.theme.MyKSuiteTheme
import com.infomaniak.core.ksuite.myksuite.ui.theme.Dimens as KSuiteDimens

@Composable
internal fun ExpandableActionItem(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    expandedView: (@Composable () -> Unit)? = null,
) {

    var isExpanded by remember { mutableStateOf(false) }

    val localColors = LocalMyKSuiteColors.current

    Column {
        Row(
            modifier = Modifier
                .heightIn(min = KSuiteDimens.textItemMinHeight)
                .fillMaxWidth()
                .then(  // TODO add onClickLabel for accessibility
                    if (expandedView == null) Modifier else Modifier.clickable { isExpanded = !isExpanded }
                )
                .padding(horizontal = Margin.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
        ) {
            Icon(
                modifier = Modifier.size(Dimens.smallIconSize),
                imageVector = ImageVector.vectorResource(iconRes),
                contentDescription = null,
                tint = localColors.iconColor,
            )
            Text(
                modifier = Modifier.weight(1.0f),
                text = stringResource(textRes),
                style = Typography.bodyRegular,
                color = localColors.primaryTextColor,
            )
            expandedView?.let {
                val icon = ImageVector.vectorResource(if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down)
                Icon(
                    imageVector = icon,
                    contentDescription = null, // TODO
                    tint = localColors.iconColor,
                )
            }
        }
        AnimatedVisibility(isExpanded) { expandedView?.invoke() }
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            Column {
                ExpandableActionItem(R.drawable.ic_padlock, R.string.myKSuiteDashboardLimitedFunctionalityLabel)
                ExpandableActionItem(
                    iconRes = R.drawable.ic_padlock,
                    textRes = R.string.myKSuiteDashboardLimitedFunctionalityLabel,
                    expandedView = { Text("") }
                )
            }
        }
    }
}
