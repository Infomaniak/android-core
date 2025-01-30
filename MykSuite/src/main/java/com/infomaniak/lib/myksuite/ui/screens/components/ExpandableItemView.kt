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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.infomaniak.lib.myksuite.R
import com.infomaniak.lib.myksuite.ui.theme.Margin
import com.infomaniak.lib.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun ExpendableActionItem(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    expendedView: (@Composable () -> Unit)? = null,
) {

    var isExpanded by remember { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .run {
                    expendedView?.let {
                        clickable(onClickLabel = "Show the limited functionalities") { // TODO put contentDesc in string
                            isExpanded = !isExpanded
                        }
                    } ?: this
                }
                .padding(horizontal = Margin.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = ImageVector.vectorResource(iconRes),
                contentDescription = null,
                tint = MyKSuiteTheme.colors.iconColor,
            )
            Spacer(Modifier.padding(Margin.Mini))
            Text(
                modifier = Modifier
                    .weight(1.0f),
                text = stringResource(textRes),
                style = MyKSuiteTheme.typography.bodyRegular,
            )
            expendedView?.let {
                Icon(
                    imageVector = ImageVector.vectorResource(if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                    contentDescription = null, // TODO
                )
            }
        }
        AnimatedVisibility(isExpanded) { expendedView?.invoke() }
    }
}
