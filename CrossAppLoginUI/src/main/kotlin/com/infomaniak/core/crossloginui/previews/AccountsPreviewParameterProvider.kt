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
package com.infomaniak.core.crossloginui.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.core.crossloginui.data.CrossLoginUiAccount

class AccountsPreviewParameter : PreviewParameterProvider<List<CrossLoginUiAccount>> {
    override val values: Sequence<List<CrossLoginUiAccount>> = sequenceOf(accountsPreviewData)
}

val accountsPreviewData = listOf(
    CrossLoginUiAccount(
        name = "Jane Doe",
        initials = "JD",
        email = "jane.doe@ik.me",
        avatarUrl = "https://picsum.photos/id/237/200/200",
        isSelected = true,
    ),
    CrossLoginUiAccount(
        name = "John Doe",
        initials = "JD",
        email = "john.doe@ik.me",
        avatarUrl = "https://picsum.photos/id/3/200/200",
        isSelected = false,
    ),
    CrossLoginUiAccount(
        name = "Gibran Cocodes",
        initials = "GC",
        email = "gibran.cocodes@ik.me",
        avatarUrl = "https://picsum.photos/id/10/200/200",
        isSelected = true,
    ),
    CrossLoginUiAccount(
        name = "Kevin Mâchicoulis",
        initials = "KM",
        email = "kevin.mâchicoulis@ik.me",
        avatarUrl = "https://picsum.photos/id/15/200/200",
        isSelected = true,
    ),
    CrossLoginUiAccount(
        name = "Tommy Del Sol",
        initials = "TD",
        email = "tommy.delsol@ik.me",
        avatarUrl = "https://picsum.photos/id/20/200/200",
        isSelected = true,
    ),
    CrossLoginUiAccount(
        name = "Gigi La Malice",
        initials = "GL",
        email = "gigi.lamalice@ik.me",
        avatarUrl = "https://picsum.photos/id/120/200/200",
        isSelected = true,
    ),
    CrossLoginUiAccount(
        name = "Morgane Doe",
        initials = "MD",
        email = "morgane.doe@ik.me",
        avatarUrl = "https://picsum.photos/id/140/200/200",
        isSelected = false,
    ),
    CrossLoginUiAccount(
        name = "Nicolas Cocodes",
        initials = "NC",
        email = "nicolas.cocodes@ik.me",
        avatarUrl = "https://picsum.photos/id/160/200/200",
        isSelected = true,
    ),
    CrossLoginUiAccount(
        name = "Kevin Deux",
        initials = "KD",
        email = "kevin.deux@ik.me",
        avatarUrl = "https://picsum.photos/id/180/200/200",
        isSelected = true,
    ),
    CrossLoginUiAccount(
        name = "Tommy Trois",
        initials = "TT",
        email = "tommy.trois@ik.me",
        avatarUrl = "https://picsum.photos/id/220/200/200",
        isSelected = true,
    ),
)
