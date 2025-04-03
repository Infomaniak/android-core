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
package com.infomaniak.core.myksuite.ui.components

import android.content.res.Configuration
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.myksuite.R
import com.infomaniak.core.myksuite.ui.theme.LocalMyKSuiteColors
import com.infomaniak.core.myksuite.ui.theme.Margin
import com.infomaniak.core.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun MyKSuiteChip(modifier: Modifier = Modifier, tier: MyKSuiteTier, @ColorInt backgroundColor: Int? = null) {
    Image(
        modifier = modifier
            .background(
                color = backgroundColor?.let(::Color) ?: LocalMyKSuiteColors.current.chipBackground,
                shape = CircleShape,
            )
            .padding(horizontal = Margin.Mini, vertical = Margin.Micro),
        imageVector = ImageVector.vectorResource(tier.iconRes),
        contentDescription = stringResource(tier.descriptionName),
    )
}

enum class MyKSuiteTier(@DrawableRes val iconRes: Int, @StringRes val descriptionName: Int) {
    Free(iconRes = R.drawable.ic_logo_my_ksuite, descriptionName = R.string.myKSuiteName),
    Plus(iconRes = R.drawable.ic_logo_my_ksuite_plus, descriptionName = R.string.myKSuitePlusName),
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(Margin.Micro)) {
                MyKSuiteChip(tier = MyKSuiteTier.Free)
                MyKSuiteChip(tier = MyKSuiteTier.Plus)
            }
        }
    }
}
