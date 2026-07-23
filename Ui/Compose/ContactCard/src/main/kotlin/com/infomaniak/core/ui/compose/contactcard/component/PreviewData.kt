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

import com.infomaniak.core.auth.models.user.Card
import com.infomaniak.core.auth.models.user.CardLink
import com.infomaniak.core.auth.models.user.CardLinkType
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.models.user.preferences.OrganizationPreference
import com.infomaniak.core.auth.models.user.preferences.Preferences

internal fun previewUser(): User {
    val orgPreference = OrganizationPreference(currentOrganizationId = 0)
    val preferences = Preferences(
        security = null,
        organizationPreference = orgPreference,
    )
    return User(
        id = 42,
        displayName = "Alice Doe",
        firstname = "Alice",
        lastname = "Doe",
        email = "alice.doe@example.com",
        avatar = "https://example.com/avatar.png",
        card = null,
        login = "alice.doe",
        isStaff = false,
        preferences = preferences,
    )
}

internal fun previewCard(): Card = Card(
    firstName = "Alice",
    lastName = "Doe",
    email = "alice.doe@example.com",
    phone = "+41 79 123 45 67",
    company = "Infomaniak",
    avatarUrl = "https://example.com/avatar.png",
    links = listOf(
        CardLink(CardLinkType.LinkedIn, "https://linkedin.com/in/alicedoe"),
        CardLink(CardLinkType.Website, "https://example.com"),
        CardLink(CardLinkType.Other, "https://blog.example.com"),
    ),
)
