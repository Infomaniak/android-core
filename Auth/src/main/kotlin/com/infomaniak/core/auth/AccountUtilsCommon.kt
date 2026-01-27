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
import android.database.sqlite.SQLiteConstraintException
import androidx.annotation.CallSuper
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.AssociatedUserDataCleanable

private const val NO_USER = -1

abstract class AccountUtilsCommon(
    context: Context,
    private val userDataCleanableList: List<AssociatedUserDataCleanable> = emptyList(),
    inMemory: Boolean = false,
) : CredentialManager() {
    final override val userDatabase: UserDatabase = UserDatabase.instantiateDataBase(context, inMemory)

    val allUsers = userDatabase.userDao().allUsers

    @Deprecated("Use currentUserFlow instead")
    final override var currentUser: User?
        get() = null
        set(value) {}

    @Deprecated("Use currentUserIdFlow instead")
    final override val currentUserId: Int get() = NO_USER

    /**
     * @throws SQLiteConstraintException when adding a user with a primary key that already exists
     */
    @CallSuper
    open suspend fun addUser(user: User) {
        val userId = user.id.toLong()
        userDataCleanableList.forEach { it.resetForUser(userId) }
        userDatabase.userDao().insert(user)
    }

    @CallSuper
    open suspend fun removeUser(userId: Int) {
        userDataCleanableList.forEach { it.resetForUser(userId.toLong()) }
        userDatabase.userDao().deleteUserById(userId)
    }
}
