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
package com.infomaniak.core.avatar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infomaniak.core.avatar.models.AvatarColors
import com.infomaniak.core.avatar.models.AvatarType

@Composable
internal fun InitialsAvatar(avatarType: AvatarType.WithInitials) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(avatarType.colors.containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(0.66f),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                modifier = Modifier.wrapContentSize(align = Alignment.Center),
                text = avatarType.initials,
                color = ColorProducer { avatarType.colors.contentColor },
                autoSize = TextAutoSize.StepBased(8.sp, stepSize = 2.sp),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    @Composable
    fun Section(text: String) {
        Text(text, fontSize = 10.sp, lineHeight = 10.sp, modifier = Modifier.padding(top = 8.dp))
    }

    MaterialTheme {
        Surface {
            Column {
                Section("24.dp")
                Box(modifier = Modifier.size(24.dp)) {
                    InitialsAvatar(AvatarType.WithInitials.Initials("WW", AvatarColors(Color(0xFF3CB572), Color.White)))
                }
                Box(modifier = Modifier.size(24.dp)) {
                    InitialsAvatar(AvatarType.WithInitials.Initials("II", AvatarColors(Color(0xFF3CB572), Color.White)))
                }

                Section("32.dp")
                Box(modifier = Modifier.size(32.dp)) {
                    InitialsAvatar(AvatarType.WithInitials.Initials("WW", AvatarColors(Color(0xFF3CB572), Color.White)))
                }
                Box(modifier = Modifier.size(32.dp)) {
                    InitialsAvatar(AvatarType.WithInitials.Initials("II", AvatarColors(Color(0xFF3CB572), Color.White)))
                }

                Section("40.dp")
                Box(modifier = Modifier.size(40.dp)) {
                    InitialsAvatar(AvatarType.WithInitials.Initials("WW", AvatarColors(Color(0xFF3CB572), Color.White)))
                }
                Box(modifier = Modifier.size(40.dp)) {
                    InitialsAvatar(AvatarType.WithInitials.Initials("II", AvatarColors(Color(0xFF3CB572), Color.White)))
                }

                Section("80.dp")
                Box(modifier = Modifier.size(80.dp)) {
                    InitialsAvatar(AvatarType.WithInitials.Initials("WW", AvatarColors(Color(0xFF3CB572), Color.White)))
                }
                Box(modifier = Modifier.size(80.dp)) {
                    InitialsAvatar(AvatarType.WithInitials.Initials("II", AvatarColors(Color(0xFF3CB572), Color.White)))
                }
            }
        }
    }
}
