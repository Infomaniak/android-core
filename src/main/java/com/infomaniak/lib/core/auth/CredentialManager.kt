/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.auth

import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.infomaniak.lib.core.BuildConfig
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.room.UserDatabase
import com.infomaniak.lib.login.ApiToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * CredentialManager interface : Implement the essential methods to get Users and their Credentials to pass
 */
abstract class CredentialManager {

    //region User
    protected abstract var userDatabase: UserDatabase
    abstract var currentUserId: Int
    abstract var currentUser: User?

    /**
     * Get users, and their informations / tokens in a JSON format
     */
    fun getAllUsers(): LiveData<List<User>> = userDatabase.userDao().getAll()

    protected fun getAllUserCount(): Int = userDatabase.userDao().count()

    suspend fun setUserToken(user: User?, apiToken: ApiToken) {
        user?.let {
            it.apiToken = apiToken
            userDatabase.userDao().update(it)
        }
    }

    suspend fun getUserById(id: Int): User? = userDatabase.userDao().findById(id)
    //endregion

    //region HttpClient
    var onRefreshTokenError: ((user: User) -> Unit)? = null

    private val mutex = Mutex()
    private var httpClientMap: ArrayMap<Pair<Int, Long?>, OkHttpClient> = ArrayMap()

    suspend fun getHttpClient(userId: Int, timeout: Long? = null): OkHttpClient {
        mutex.withLock {
            var httpClient = httpClientMap[Pair(userId, timeout)]
            if (httpClient == null) {
                httpClient = getHttpClientUser(userId, timeout)
                httpClientMap[Pair(userId, timeout)] = httpClient
            }
            return httpClient
        }
    }

    private suspend fun getHttpClientUser(userId: Int, timeout: Long?): OkHttpClient {
        var user = getUserById(userId)
        return OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addNetworkInterceptor(StethoInterceptor())
            }
            timeout?.let {
                callTimeout(timeout, TimeUnit.SECONDS)
                readTimeout(timeout, TimeUnit.SECONDS)
                writeTimeout(timeout, TimeUnit.SECONDS)
                connectTimeout(timeout, TimeUnit.SECONDS)
            }
            val tokenInterceptorListener = object : TokenInterceptorListener {
                override suspend fun onRefreshTokenSuccess(apiToken: ApiToken) {
                    setUserToken(user, apiToken)
                    if (currentUserId == userId) {
                        currentUser = user
                    }
                }

                override suspend fun onRefreshTokenError() {
                    user?.let { onRefreshTokenError?.invoke(it) }
                }

                override suspend fun getApiToken(): ApiToken {
                    user = getUserById(userId)
                    return user?.apiToken!!
                }
            }
            addInterceptor(TokenInterceptor(tokenInterceptorListener))
            authenticator(TokenAuthenticator(tokenInterceptorListener))
        }.run {
            build()
        }
    }
    //endregion
}
