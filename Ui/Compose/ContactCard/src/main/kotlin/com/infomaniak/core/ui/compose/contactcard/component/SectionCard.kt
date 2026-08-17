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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.infomaniak.core.ui.compose.margin.Margin

@Composable
internal fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = Margin.Medium + Margin.Mini,
                end = Margin.Medium,
                bottom = Margin.Mini,
            ),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin.Medium),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(CardCornerRadius),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Preview(name = "SectionCard")
@Composable
private fun SectionCardPreview() {
    MaterialTheme {
        Surface {
            SectionCard(title = "Section Title") {
                Text("Content")
            }
        }
    }
}
