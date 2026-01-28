/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2025 Infomaniak Network SA
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
package com.infomaniak.core.auth

import androidx.lifecycle.LiveData
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.lib.login.ApiToken

/**
 * CredentialManager: Adds a currentUserId and currentUser management layer to [BaseCredentialManager]
 *
 * It's now recommended to use [AccountUtilsCommon], [AbstractCurrentUserAccountUtils] or [PersistedCurrentUserAccountUtils]
 * instead as they factorize much more code and have safer current user management logic.
 */
abstract class CredentialManager : BaseCredentialManager() {

    abstract val currentUserId: Int
    abstract var currentUser: User?

    fun getAllUsers(): LiveData<List<User>> = userDatabase.userDao().getAll()

    fun getAllUsersCount(): Int = userDatabase.userDao().count()

    final override suspend fun setUserToken(user: User?, apiToken: ApiToken) {
        super.setUserToken(user, apiToken)
        user?.let { if (currentUserId == it.id) currentUser = it }
    }

    suspend fun getUserById(id: Int): User? = userDatabase.userDao().findById(id)
}
