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
import com.infomaniak.core.auth.models.CurrentUserId
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.AssociatedUserDataCleanable
import kotlinx.coroutines.flow.Flow

/**
 * A version of [AbstractCurrentUserAccountUtils] that automatically stores the current user ID inside of Room.
 */
open class PersistedCurrentUserAccountUtils(
    appContext: Context,
    userDataCleanableList: List<AssociatedUserDataCleanable> = emptyList(),
    userDatabase: UserDatabase = UserDatabase.instantiateDataBase(appContext),
) : AbstractCurrentUserAccountUtils(appContext, userDataCleanableList, userDatabase) {
    override val currentUserIdFlow: Flow<Int?> = currentUserIdDao.getCurrentUserIdFlow()

    override suspend fun setCurrentUserId(userId: Int?) {
        when (userId) {
            null -> currentUserIdDao.deleteCurrentUserId()
            else -> currentUserIdDao.setCurrentUserId(CurrentUserId(userId))
        }
    }
}
