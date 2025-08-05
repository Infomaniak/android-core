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
package com.infomaniak.core.crosslogin.front.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.core.crosslogin.back.ExternalAccount

class AccountsPreviewParameter : PreviewParameterProvider<List<ExternalAccount>> {
    override val values: Sequence<List<ExternalAccount>> = sequenceOf(accountsPreviewData)
}

val accountsPreviewData = listOf(
    ExternalAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 1,
        fullName = "Jane Doe",
        initials = "JD",
        email = "jane.doe@ik.me",
        avatarUrl = "https://picsum.photos/id/140/200/200",
    ),
    ExternalAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 2,
        fullName = "John Doe",
        initials = "JD",
        email = "john.doe@ik.me",
        avatarUrl = "https://picsum.photos/id/3/200/200",
    ),
    ExternalAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 3,
        fullName = "Gibran Cocodes",
        initials = "GC",
        email = "gibran.cocodes@ik.me",
        avatarUrl = "https://picsum.photos/id/10/200/200",
    ),
    ExternalAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 4,
        fullName = "Tommy Del Sol",
        initials = "TD",
        email = "tommy.delsol@ik.me",
        avatarUrl = "https://picsum.photos/id/20/200/200",
    ),
    ExternalAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 5,
        fullName = "Kevin Mâchicoulis",
        initials = "KM",
        email = "kevin.machicoulis@ik.me",
        avatarUrl = "https://picsum.photos/id/15/200/200",
    ),
    ExternalAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 6,
        fullName = "Gigi La Malice",
        initials = "GL",
        email = "gigi.lamalice@ik.me",
        avatarUrl = "https://picsum.photos/id/220/200/200",
    ),
)
