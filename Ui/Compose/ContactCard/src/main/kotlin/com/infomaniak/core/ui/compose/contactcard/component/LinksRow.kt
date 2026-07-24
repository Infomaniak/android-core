/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.core.ui.compose.contactcard.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.models.user.CardLink
import com.infomaniak.core.auth.models.user.CardLinkType
import com.infomaniak.core.ui.compose.contactcard.R
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
internal fun LinksRow(links: List<CardLink>, modifier: Modifier = Modifier) {
    val grouped = links.groupBy { it.type }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Margin.Small),
    ) {
        CardLinkType.entries.forEach { type ->
            val linksOfType = grouped[type] ?: return@forEach
            Icon(
                imageVector = ImageVector.vectorResource(type.iconRes()),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            if (type == CardLinkType.Other && linksOfType.size > 1) {
                Text(
                    text = "+${linksOfType.size - 1}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@DrawableRes
private fun CardLinkType.iconRes(): Int = when (this) {
    CardLinkType.LinkedIn -> R.drawable.ic_linkedin
    CardLinkType.Facebook -> R.drawable.ic_facebook
    CardLinkType.Instagram -> R.drawable.ic_instagram
    CardLinkType.X -> R.drawable.ic_x
    CardLinkType.Other, CardLinkType.Website -> R.drawable.ic_link
}

@Preview(name = "LinksRow")
@Composable
private fun LinksRowPreview() {
    MaterialTheme {
        Surface {
            LinksRow(
                links = listOf(
                    CardLink(CardLinkType.LinkedIn, "https://linkedin.com"),
                    CardLink(CardLinkType.Other, "https://blog.example.com"),
                ),
            )
        }
    }
}
