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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.avatar.components.Avatar
import com.infomaniak.core.avatar.models.AvatarType
import com.infomaniak.core.ui.compose.contactcard.R
import com.infomaniak.core.ui.compose.margin.Margin
import io.github.alexzhirkevich.qrose.QrCodePainter

@Composable
internal fun QrCodeHeader(user: User, card: Card) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val qrSize = (maxWidth * 0.62f).coerceAtMost(240.dp)
        val gradientHeight = qrSize * 0.4f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(qrSize),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gradientHeight)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }

        Surface(
            shape = RoundedCornerShape(CardCornerRadius),
            modifier = Modifier.padding(top = gradientHeight * 0.5f),
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            Box(
                modifier = Modifier
                    .size(qrSize)
                    .padding(Margin.Small),
                contentAlignment = Alignment.Center,
            ) {
                val vCardData = card.makeVCardString(forQRCode = true)
                val qrPainter = remember(vCardData) {
                    runCatching { QrCodePainter(data = vCardData) }.getOrNull()
                }
                if (qrPainter != null) {
                    Image(
                        painter = qrPainter,
                        contentDescription = stringResource(R.string.contactCardQrCodeDescription),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.contactCardQrCodeTooLarge),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(qrSize * 0.24f)
                        .clip(CircleShape),
                ) {
                    Box(
                        modifier = Modifier.padding(3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Avatar(
                            avatarType = AvatarType.fromUser(user),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "QrCodeHeader")
@Composable
private fun QrCodeHeaderPreview() {
    MaterialTheme {
        Surface {
            QrCodeHeader(
                user = previewUser(),
                card = previewCard(),
            )
        }
    }
}
