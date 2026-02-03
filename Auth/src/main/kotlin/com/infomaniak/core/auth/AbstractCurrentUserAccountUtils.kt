/*
 * Infomaniak Core - Android
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
import androidx.room.withTransaction
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.AssociatedUserDataCleanable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Extends [UserAccountUtils] by introducing the concept of a current user.
 *
 * How the current user ID is stored is left to the caller. If you do not have any specific storage requirements, use
 * [PersistedCurrentUserAccountUtils], which persists the current user using Room.
 */
abstract class AbstractCurrentUserAccountUtils(
    appContext: Context,
    userDataCleanableList: List<AssociatedUserDataCleanable> = emptyList(),
    userDatabase: UserDatabase = UserDatabase.instantiateDataBase(appContext),
) : UserAccountUtils(appContext, userDataCleanableList, userDatabase) {

    /**
     * If you need a live [User] instead of just its id, use [currentUserFlow]
     */
    abstract val currentUserIdFlow: Flow<Int?>

    /**
     * If you only need the user id, use [currentUserIdFlow] instead
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUserFlow: Flow<User?> by lazy {
        currentUserIdFlow.flatMapLatest { userId ->
            userId?.let { userDao.findByIdFlow(it) } ?: flowOf(null)
        }
    }

    /**
     * Update the currentUserIdFlow with the new [userId]
     */
    abstract suspend fun setCurrentUserId(userId: Int?)

    /**
     * Adds a new user to the list of all users and automatically selects it as the current user.
     *
     * @throws SQLiteConstraintException when adding a user with a primary key that already exists
     */
    override suspend fun addUser(user: User) {
        userDatabase.withTransaction {
            super.addUser(user)
            switchUser(user.id)
        }
    }

    /**
     * Removes definitively a user from the list of all users and if it was the current user, it will automatically switch to
     * another user in the list when available.
     */
    override suspend fun removeUser(userId: Int) {
        userDatabase.withTransaction {
            super.removeUser(userId)
            if (currentUserIdFlow.first() == userId) switchUser(getNextUserId())
        }
    }

    /**
     * Switches the current user. This method will always suspend until [currentUserFlow] and [currentUserIdFlow] has had time to
     * be updated.
     */
    @CallSuper
    open suspend fun switchUser(userId: Int?) {
        when (userId) {
            null -> setCurrentUserId(null)
            else -> switchUser(userId)
        }
    }

    /**
     * Switches the currently selected user. If the given [userId] does not exist in the user table, this method does nothing.
     */
    private suspend fun switchUser(userId: Int) {
        if (userDao.findById(userId) == null) return
        setCurrentUserId(userId)
    }

    private suspend fun getNextUserId(): Int? = userDao.getFirst()?.id
}
