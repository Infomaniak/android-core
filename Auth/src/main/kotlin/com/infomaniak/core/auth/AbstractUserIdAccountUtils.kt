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
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.common.AssociatedUserDataCleanable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

abstract class AbstractUserIdAccountUtils(
    context: Context,
    userDataCleanableList: List<AssociatedUserDataCleanable> = emptyList(),
    inMemory: Boolean = false,
) : AccountUtilsCommon(context, userDataCleanableList, inMemory) {
    // TODO: See if this can be private
    abstract val currentUserIdFlow: Flow<Int?>

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUserFlow: Flow<User?> by lazy {
        currentUserIdFlow.flatMapLatest { userId ->
            userId?.let { getUserFlowById(it) } ?: flowOf(null)
        }
    }
}
