/*
 * Infomaniak SwissTransfer - Android
 * Copyright (C) 2026 Infomaniak Network SA
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

import android.content.Context
import androidx.annotation.CallSuper
import com.infomaniak.core.AssociatedUserDataCleanable
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

// TODO: Find the name we always use
private const val NO_USER = -1

abstract class AccountUtilsCommon(
    private val userDataCleanableList: List<AssociatedUserDataCleanable> = emptyList(),
    context: Context,
) : CredentialManager() {
    override val userDatabase: UserDatabase = UserDatabase.instantiateDataBase(context)

    abstract val currentUserIdFlow: StateFlow<Int?>
    abstract val currentUserFlow: StateFlow<User?> = currentUserIdFlow.map {
        getUserById()
    }

    override val currentUser: User? get() = currentUserFlow.value
    override val currentUserId: Int get() = currentUserIdFlow.value ?: NO_USER

    @CallSuper
    open suspend fun addUser() {
        userDatabase
    }

    @CallSuper
    open suspend fun removeUser() {

    }
}
