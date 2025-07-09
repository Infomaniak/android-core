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
import com.infomaniak.core.crossloginui.data.CrossLoginAccount

class AccountsPreviewParameter : PreviewParameterProvider<List<CrossLoginAccount>> {
    override val values: Sequence<List<CrossLoginAccount>> = sequenceOf(accountsPreviewData)
}

val accountsPreviewData = listOf(
    CrossLoginAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 1,
        name = "Jane Doe",
        initials = "JD",
        email = "jane.doe@ik.me",
        url = "https://picsum.photos/id/140/200/200",
    ),
    CrossLoginAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 2,
        name = "John Doe",
        initials = "JD",
        email = "john.doe@ik.me",
        url = "https://picsum.photos/id/3/200/200",
    ),
    CrossLoginAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 3,
        name = "Gibran Cocodes",
        initials = "GC",
        email = "gibran.cocodes@ik.me",
        url = "https://picsum.photos/id/10/200/200",
    ),
    CrossLoginAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 4,
        name = "Tommy Del Sol",
        initials = "TD",
        email = "tommy.delsol@ik.me",
        url = "https://picsum.photos/id/20/200/200",
    ),
    CrossLoginAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 5,
        name = "Kevin Mâchicoulis",
        initials = "KM",
        email = "kevin.machicoulis@ik.me",
        url = "https://picsum.photos/id/15/200/200",
    ),
    CrossLoginAccount(
        tokens = setOf("I'm a token"),
        isCurrentlySelectedInAnApp = false,
        id = 6,
        name = "Gigi La Malice",
        initials = "GL",
        email = "gigi.lamalice@ik.me",
        url = "https://picsum.photos/id/220/200/200",
    ),
)
