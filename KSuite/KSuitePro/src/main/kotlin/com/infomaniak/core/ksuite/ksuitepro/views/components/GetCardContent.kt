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
package com.infomaniak.core.ksuite.ksuitepro.views.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.ui.compose.basics.Dimens
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ksuite.ksuitepro.R
import com.infomaniak.core.ksuite.ui.components.kSuiteGradient

@Composable
internal fun GetCardContent(
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = kSuiteGradient(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.kSuiteProBackground)),
        shape = RoundedCornerShape(Dimens.largeCornerRadius),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(all = Margin.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Margin.Medium),
        ) {
            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Margin.Micro)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Margin.Micro),
                ) {
                    Text(
                        text = stringResource(R.string.kSuiteGetProTitle),
                        style = Typography.bodyMedium,
                        color = colorResource(R.color.swan),
                    )
                    Image(
                        modifier = Modifier.size(width = Dimens.avatarSize, height = Dimens.smallIconSize),
                        painter = painterResource(R.drawable.ic_ksuite_pro),
                        contentDescription = null,
                    )
                }
                Text(
                    text = stringResource(R.string.kSuiteGetProDescription),
                    style = Typography.bodySmallRegular,
                    color = colorResource(R.color.mouse),
                )
            }
            Icon(
                modifier = Modifier.size(Dimens.smallIconSize),
                imageVector = ImageVector.vectorResource(R.drawable.ic_chevron),
                tint = colorResource(R.color.swan),
                contentDescription = null,
            )
        }
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    Surface {
        GetCardContent(onClick = {})
    }
}
