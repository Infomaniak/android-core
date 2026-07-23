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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.CardLinkType
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
internal fun ContactVCardBloc(user: User, card: Card) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            QrCodeHeader(user = user, card = card)
            Spacer(Modifier.height(Margin.Medium))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin.Medium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${card.firstName} ${card.lastName}".trim(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Margin.Mini))
                Text(
                    text = card.email,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(Margin.Medium))

            ContactInfoRows(
                card = card,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Margin.Medium),
            )

            val links = card.links.orEmpty().filter { it.url.isNotBlank() && it.type != CardLinkType.Website }
            if (links.isNotEmpty()) {
                Spacer(Modifier.height(Margin.Medium))
                LinksRow(
                    links = links,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Margin.Medium, vertical = Margin.Small),
                )
            } else {
                Spacer(Modifier.height(Margin.Medium))
            }
        }
    }
}

@Preview(name = "ContactVCardBloc")
@Composable
private fun ContactVCardBlocPreview() {
    MaterialTheme {
        Surface {
            ContactVCardBloc(
                user = previewUser(),
                card = previewCard(),
            )
        }
    }
}
