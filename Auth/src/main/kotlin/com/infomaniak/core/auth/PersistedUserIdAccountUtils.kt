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
import com.infomaniak.core.auth.models.CurrentUserId
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.common.AssociatedUserDataCleanable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * A version of [AccountUtilsCommon] that automatically stores the current user id inside of room as well
 */
abstract class PersistedUserIdAccountUtils(
    context: Context,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    inMemory: Boolean = false,
    userDataCleanableList: List<AssociatedUserDataCleanable> = emptyList(),
) : AccountUtilsCommon(context, coroutineScope, inMemory, userDataCleanableList) {
    override val currentUserIdFlow: Flow<Int?> = userDatabase
        .currentUserIdDao()
        .getCurrentUserIdFlow()

    override suspend fun addUser(user: User) {
        super.addUser(user)
        userDatabase.currentUserIdDao().setCurrentUserId(CurrentUserId(user.id))
    }

    override suspend fun removeUser(userId: Int) {
        super.removeUser(userId)
        removeCurrentUserIdIfSelected(userId)
    }

    suspend fun removeUserAndSwitchToNext(userId: Int) {
        super.removeUser(userId)

        getNextUserId()?.let { nextUserId ->
            switchUser(nextUserId)
        } ?: run {
            removeCurrentUserIdIfSelected(userId)
        }
    }

    /**
     * Switches the currently selected user. If the given [userId] does not exist in the user table, this method does nothing.
     */
    @CallSuper
    open suspend fun switchUser(userId: Int) {
        if (userDatabase.userDao().findById(userId) == null) return
        userDatabase.currentUserIdDao().setCurrentUserId(CurrentUserId(userId))
    }

    private suspend fun removeCurrentUserIdIfSelected(userId: Int) {
        val currentUserIdDao = userDatabase.currentUserIdDao()
        if (currentUserIdDao.getCurrentUserIdFlow().first() == userId) {
            currentUserIdDao.clearCurrentUserId()
        }
    }

    private suspend fun getNextUserId(): Int? = userDatabase.userDao().getFirst()?.id
}
