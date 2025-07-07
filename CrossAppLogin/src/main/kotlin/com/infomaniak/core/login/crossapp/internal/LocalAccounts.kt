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
package com.infomaniak.core.login.crossapp.internal

import com.infomaniak.core.login.crossapp.ExternalAccount
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.room.UserDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
internal fun localAccountsFlow(currentUserIdFlow: Flow<Int>): Flow<List<ExternalAccount>> {
    return combine(
        UserDatabase().userDao().allUsers,
        currentUserIdFlow
    ) { allUsers: List<User>, currentUserId: Int ->
        if (allUsers.none { it.isStaff }) emptyList() else allUsers.map { user ->
            //TODO[CrossAppLogin]: Drop the isStaff condition after successful testing.
            ExternalAccount(
                fullName = user.displayName ?: user.run { "$firstname $lastname" },
                email = user.email,
                avatarUrl = user.avatar,
                isCurrentlySelectedInAnApp = user.id == currentUserId,
                tokens = setOf(user.apiToken.accessToken),
                initials = user.getInitials(),
                id = user.id,
            )
        }
    }
}
