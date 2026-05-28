/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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
package com.infomaniak.core.ui.compose.preview.previewparameter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.models.user.preferences.OrganizationPreference
import com.infomaniak.core.auth.models.user.preferences.Preferences
import com.infomaniak.core.login.ApiToken

class UserListPreviewParameterProvider : PreviewParameterProvider<List<User>> {
    override val values: Sequence<List<User>> = sequenceOf(usersPreviewData)
}

private val usersPreviewData = listOf(
    dummyUserOf(1, "Alice", "Doe"),
    dummyUserOf(2, "Bob", "Collins"),
)

fun dummyUserOf(id: Int, firstName: String, lastName: String): User {
    val dummyOrganization = OrganizationPreference(1234)
    val dummyPreferences = Preferences(null, dummyOrganization)
    val dummyApiToken = ApiToken("", null, "", 0, id, null, null)

    return User(
        id = id,
        displayName = "$firstName $lastName",
        firstname = firstName,
        lastname = lastName,
        email = "$firstName.$lastName@infomaniak.com".lowercase(),
        avatar = null,
        login = "",
        isStaff = false,
        preferences = dummyPreferences,
        apiToken = dummyApiToken,
        organizations = ArrayList(),
    )
}
