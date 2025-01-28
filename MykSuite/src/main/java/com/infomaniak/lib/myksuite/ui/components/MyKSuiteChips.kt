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
package com.infomaniak.lib.myksuite.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.lib.myksuite.R
import com.infomaniak.lib.myksuite.ui.theme.Dimens
import com.infomaniak.lib.myksuite.ui.theme.MyKSuiteTheme

@Composable
fun MyKSuitePlusChip(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ThemedChip(
        modifier = modifier,
        label = { Image(ImageVector.vectorResource(R.drawable.ic_logo_my_ksuite_plus), contentDescription = "My kSuite +") },
        onClick = onClick,
    )
}

@Composable
fun MyKSuiteChip(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ThemedChip(
        modifier = modifier,
        label = { Image(ImageVector.vectorResource(R.drawable.ic_logo_my_ksuite), contentDescription = "My kSuite") },
        onClick = onClick,
    )
}

@Composable
private fun ThemedChip(modifier: Modifier = Modifier, label: @Composable () -> Unit, onClick: () -> Unit = {}) {
    MyKSuiteTheme {
        SuggestionChip(
            modifier = modifier,
            onClick = onClick,
            label = label,
            border = null,
            shape = RoundedCornerShape(Dimens.largeCornerRadius),
            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MyKSuiteTheme.colors.chipBackground),
        )
    }
}

@Preview(name = "(1) Light")
@Preview(name = "(2) Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    MyKSuiteTheme {
        Surface {
            Column {
                MyKSuitePlusChip {}
                MyKSuiteChip {}
            }
        }
    }
}
