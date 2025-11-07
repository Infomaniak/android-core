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
package com.infomaniak.core.crossapplogin.front.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.ui.compose.basics.Dimens
import com.infomaniak.core.ui.compose.basics.Typography
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.crossapplogin.front.R
import com.infomaniak.core.crossapplogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crossapplogin.front.icons.AddUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddAccount(
    customization: CrossLoginCustomization,
    contentPadding: PaddingValues = PaddingValues(horizontal = Margin.Medium),
    onClick: () -> Unit,
) {
    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.buttonHeight),
        shape = RectangleShape,
        onClick = onClick,
        contentPadding = contentPadding,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Margin.Mini),
            horizontalArrangement = Arrangement.spacedBy(Margin.Mini),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Icon(
                modifier = Modifier
                    .padding(all = Margin.Mini)
                    .size(size = Dimens.iconSize),
                imageVector = AddUser,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null,
            )

            Text(
                text = stringResource(R.string.buttonUseOtherAccount),
                style = Typography.bodyRegular,
                color = customization.colors.titleColor,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            AddAccount(
                customization = CrossLoginDefaults.customize(),
                onClick = {},
            )
        }
    }
}
