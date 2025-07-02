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
package com.infomaniak.core.crossloginui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossloginui.data.CrossLoginColors
import com.infomaniak.core.crossloginui.icons.Checkmark
import com.infomaniak.core.crossloginui.theme.Dimens
import com.infomaniak.core.crossloginui.theme.Typography

@Composable
fun AccountItem(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    iconUrl: String? = null,
    iconsUrls: List<String>? = null,
    endIcon: ImageVector = Checkmark,
    hasBorder: Boolean = false,
    colors: CrossLoginColors,
    isSelected: (() -> Boolean)? = null,
    onClick: () -> Unit,
) {
    SharpRippleButton(
        isSelected = isSelected ?: { false },
        borderColor = if (hasBorder) colors.buttonStrokeColor else null,
        onClick = onClick,
    ) {
        AccountItemContent(title, description, icon, iconUrl, iconsUrls, endIcon, colors, isSelected)
    }
}

@Composable
private fun AccountItemContent(
    title: String,
    description: String?,
    icon: ImageVector?,
    iconUrl: String?,
    iconsUrls: List<String>?,
    endIcon: ImageVector,
    colors: CrossLoginColors,
    isSelected: (() -> Boolean)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin.Medium, vertical = Margin.Mini),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        DisplayIcons(icon, iconUrl, iconsUrls, colors)

        Spacer(Modifier.width(Margin.Mini))

        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = title,
                style = if (icon == null) Typography.bodyMedium else Typography.bodyRegular,
                color = colors.titleColor,
            )
            description?.let {
                Text(
                    text = it,
                    style = Typography.bodyRegular,
                    color = colors.descriptionColor,
                )
            }
        }

        if (isSelected?.invoke() == true) {
            Icon(
                imageVector = endIcon,
                contentDescription = null,
                tint = colors.primaryColor,
            )
        }
    }
}

@Composable
private fun DisplayIcons(icon: ImageVector?, iconUrl: String?, iconsUrls: List<String>?, colors: CrossLoginColors) {
    when {
        icon != null -> Box(Modifier.padding(all = Margin.Mini)) {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(Dimens.iconSize),
                contentDescription = null,
                tint = colors.primaryColor,
            )
        }
        iconUrl != null -> AsyncImage(
            model = iconUrl,
            modifier = Modifier
                .size(Dimens.bigIconSize)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
        iconsUrls != null && iconsUrls.count() == 2 -> TwoAvatarsView(urls = iconsUrls, colors = colors)
        iconsUrls != null && iconsUrls.count() > 2 -> ThreeAvatarsView(urls = iconsUrls, colors = colors)
    }
}
