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
package com.infomaniak.core.twofactorauth.back

import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient

fun TwoFactorAuthManager(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    getConnectedHttpClient: suspend (userId: Int) -> OkHttpClient
): TwoFactorAuthManager = TwoFactorAuthManager(
    coroutineScope = coroutineScope,
    userIds = UserDatabase().userDao().allUsers.map { users ->
        users.mapTo(hashSetOf()) { it.id }
    }.distinctUntilChanged(),
    getAccountInfo = { UserDatabase().userDao().findById(it)?.toTargetAccount() },
    getConnectedHttpClient = getConnectedHttpClient
)

private fun User.toTargetAccount() = ConnectionAttemptInfo.TargetAccount(
    avatarUrl = avatar,
    fullName = displayName ?: run { "$firstname $lastname" },
    initials = getInitials(),
    email = email,
    id = id.toLong(),
)
