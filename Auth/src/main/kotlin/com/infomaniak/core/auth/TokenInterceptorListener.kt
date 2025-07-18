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

import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.lib.login.ApiToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn

interface TokenInterceptorListener {
    suspend fun onRefreshTokenSuccess(apiToken: ApiToken)
    suspend fun onRefreshTokenError()
    suspend fun getApiToken(): ApiToken?
    fun getCurrentUserId(): Int?

    /**
     * Maps a flow of user IDs to a shared flow of API tokens with caching.
     *
     * Uses a shared flow with `replay = 1` to cache the latest emitted API token.
     *
     * @param coroutineScope Scope used to share the resulting flow.
     * @return Shared flow emitting API tokens corresponding to the user IDs.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun Flow<Int?>.mapToApiToken(coroutineScope: CoroutineScope) = this
        .mapLatest { id -> id?.let { UserDatabase.getDatabase().userDao().findById(it)?.apiToken } }
        .catch {
            // Let user retry without logging him out.
            // If we send null, the user will be logged out.
        }
        .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}
