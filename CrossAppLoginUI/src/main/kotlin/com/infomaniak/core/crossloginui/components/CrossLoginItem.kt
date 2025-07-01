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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.infomaniak.core.crossloginui.icons.Checkmark
import com.infomaniak.core.crossloginui.theme.LocalCrossLoginColors
import com.infomaniak.core.crossloginui.theme.Typography

private val ITEM_MIN_HEIGHT = 56.dp

@Composable
fun CrossLoginItem(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    iconUrl: String? = null,
    iconsUrls: List<String>? = null,
    isSelected: (() -> Boolean)? = null,
    onClick: () -> Unit,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = ITEM_MIN_HEIGHT)

    SharpRippleButton(
        modifier = modifier,
        isSelected = isSelected ?: { false },
        onClick = onClick,
    ) {
        CrossLoginItemContent(title, description, icon, iconUrl, iconsUrls, isSelected)
    }
}

@Composable
private fun CrossLoginItemContent(
    title: String,
    description: String?,
    icon: ImageVector?,
    iconUrl: String?,
    iconsUrls: List<String>?,
    isSelected: (() -> Boolean)?,
) {

    val localColors = LocalCrossLoginColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Margin.Medium, vertical = Margin.Small),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        DisplayIcons(icon, iconUrl, iconsUrls)

        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = title,
                style = Typography.bodyRegular,
                color = localColors.primaryTextColor,
            )
            description?.let {
                Text(
                    text = it,
                    style = Typography.bodySmallRegular,
                    color = localColors.secondaryTextColor,
                )
            }
        }

        if (isSelected?.invoke() == true) {
            Icon(
                imageVector = Checkmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DisplayIcons(icon: ImageVector?, iconUrl: String?, iconsUrls: List<String>?) {

    val displayedIcons = when {
        icon != null -> {
            @Composable {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    // tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        iconUrl != null -> {
            @Composable {
                AsyncImage(
                    model = iconUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
        }
        iconsUrls != null -> {
            // @Composable { AvatarsStackView(iconsUrls) }
            if (iconsUrls.count() == 2) {
                @Composable { TwoAvatarsView(iconsUrls) }
            } else {
                @Composable { ThreeAvatarsView(iconsUrls) }
            }
        }
        else -> null
    }

    displayedIcons?.let {
        Box(Modifier.width(48.dp)) {
            it()
        }
        Spacer(Modifier.width(Margin.Medium))
    }
}
