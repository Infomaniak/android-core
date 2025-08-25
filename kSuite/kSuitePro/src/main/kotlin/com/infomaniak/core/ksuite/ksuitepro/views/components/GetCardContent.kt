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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.basics.Dimens
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.ksuite.ksuitepro.R
import com.infomaniak.core.ksuite.ksuitepro.utils.KSuiteProUiUtils.color
import com.infomaniak.core.ksuite.ui.components.kSuiteGradient

@Composable
fun GetCardContent(
    onClick: () -> Unit,
) {

    val resources = LocalResources.current

    Card(
        modifier = Modifier.height(76.dp),
        border = kSuiteGradient(),
        colors = CardDefaults.cardColors(containerColor = resources.color(R.color.kSuiteBackground)),
        shape = RoundedCornerShape(Dimens.largeCornerRadius),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = Margin.Medium)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.kSuiteGetProTitle),
                        style = Typography.bodyMedium,
                        color = resources.color(R.color.swan),
                    )
                    Spacer(Modifier.width(Margin.Micro))
                    Image(
                        modifier = Modifier.size(width = Dimens.avatarSize, height = Dimens.smallIconSize),
                        painter = painterResource(R.drawable.ic_ksuite_pro),
                        contentDescription = null,
                    )
                }
                Spacer(Modifier.height(Margin.Micro))
                Text(
                    text = stringResource(R.string.kSuiteGetProDescription),
                    style = Typography.bodySmallRegular,
                    color = resources.color(R.color.mouse),
                )
            }
            Spacer(Modifier.width(Margin.Medium))
            Image(
                colorFilter = ColorFilter.tint(resources.color(R.color.swan)),
                modifier = Modifier.size(Dimens.smallIconSize),
                painter = painterResource(R.drawable.ic_ksuite_chevron),
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
