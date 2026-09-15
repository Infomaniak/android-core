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
@file:OptIn(ExperimentalTypeInference::class)

package com.infomaniak.core.twofactorauth.back

import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.DynamicLazyMap
import com.infomaniak.core.common.dynamicLazyMap
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import kotlin.experimental.ExperimentalTypeInference

fun TwoFactorAuthManager(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    getConnectedHttpClient: suspend (userId: Int) -> OkHttpClient
): TwoFactorAuthManager = TwoFactorAuthManager(
    coroutineScope = coroutineScope,
    connectedHttpClients = coroutineScope.dynamicLazyMap { userId ->
        async { getConnectedHttpClient(userId.toInt()).toKtorClient() }
    }
)

fun TwoFactorAuthManager(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    connectedHttpClients: DynamicLazyMap<Long, Deferred<HttpClient>>,
): TwoFactorAuthManager = TwoFactorAuthManager(
    coroutineScope = coroutineScope,
    userIds = UserDatabase().userDao().allUsers.map { users ->
        users.mapTo(hashSetOf()) { it.id.toLong() }
    }.distinctUntilChanged(),
    getAccountInfo = { UserDatabase().userDao().findById(it)?.toTargetAccount() },
    perUserHttpClient = connectedHttpClients
)

private fun User.toTargetAccount() = ConnectionAttemptInfo.TargetAccount(
    avatarUrl = avatar,
    fullName = displayName ?: run { "$firstname $lastname" },
    initials = getInitials(),
    email = email,
    id = id.toLong(),
)

private fun OkHttpClient.toKtorClient(): HttpClient = HttpClient(OkHttp) { engine { preconfigured = this@toKtorClient } }
