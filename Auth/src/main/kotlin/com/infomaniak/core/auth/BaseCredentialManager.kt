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

import androidx.annotation.CallSuper
import androidx.collection.ArrayMap
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.network.networking.HttpClientConfig
import com.infomaniak.lib.login.ApiToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * BaseCredentialManager: Implement the essential methods to get Users and their Credentials to pass
 */
abstract class BaseCredentialManager : UserExistenceChecker {

    protected abstract val userDatabase: UserDatabase

    override suspend fun isUserAlreadyPresent(userId: Int): Boolean = userDatabase.userDao().findById(userId) != null

    //region Helper
    protected val userDao get() = userDatabase.userDao()
    protected val currentUserIdDao get() = userDatabase.currentUserIdDao()
    //endregion

    //region User
    @CallSuper
    open suspend fun setUserToken(user: User?, apiToken: ApiToken) {
        user?.let {
            it.apiToken = apiToken
            userDatabase.userDao().update(it)
        }
    }
    //endregion

    //region HttpClient
    var onRefreshTokenError: ((user: User) -> Unit)? = null

    private val mutex = Mutex()
    private val httpClientMap: ArrayMap<Pair<Int, Long?>, OkHttpClient> = ArrayMap()

    val defaultTokenAuthenticatorFactory: (TokenInterceptorListener) -> TokenAuthenticator = { tokenInterceptorListener ->
        TokenAuthenticator(tokenInterceptorListener)
    }

    val defaultTokenInterceptorFactory: (TokenInterceptorListener) -> TokenInterceptor = { tokenInterceptorListener ->
        TokenInterceptor(tokenInterceptorListener)
    }

    suspend fun getHttpClient(
        userId: Int,
        timeout: Long? = null,
        getAuthenticator: ((TokenInterceptorListener) -> TokenAuthenticator)? = defaultTokenAuthenticatorFactory,
        getInterceptor: (TokenInterceptorListener) -> Interceptor = defaultTokenInterceptorFactory,
    ): OkHttpClient {
        mutex.withLock {
            var httpClient = httpClientMap[Pair(userId, timeout)]
            if (httpClient == null) {
                httpClient = getHttpClientUser(userId, timeout, getAuthenticator, getInterceptor)
                httpClientMap[Pair(userId, timeout)] = httpClient
            }
            return httpClient
        }
    }

    private suspend fun getHttpClientUser(
        userId: Int,
        timeout: Long?,
        getAuthenticator: ((TokenInterceptorListener) -> TokenAuthenticator)?,
        getInterceptor: (TokenInterceptorListener) -> Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            timeout?.let {
                // NEVER set `callTimeout` to a too low value, because it would break users on slow connexions.
                // Think hours or minutes for uploads, and tens of seconds for other calls.
                readTimeout(timeout, TimeUnit.SECONDS)
                writeTimeout(timeout, TimeUnit.SECONDS)
                connectTimeout(timeout, TimeUnit.SECONDS)
            }

            val tokenInterceptorListener = getDefaultTokenInterceptorListener(userId)

            HttpClientConfig.apply { cacheDir?.let { cache(Cache(it, CACHE_SIZE_BYTES)) } }
            addInterceptor(getInterceptor(tokenInterceptorListener))
            getAuthenticator?.let { tokenAuthenticator ->
                authenticator(tokenAuthenticator(tokenInterceptorListener))
            }

            HttpClientConfig.addCommonInterceptors(this) // Needs to be added last
        }.run {
            build()
        }
    }

    private suspend fun getDefaultTokenInterceptorListener(userId: Int): TokenInterceptorListener {
        var user = userDatabase.userDao().findById(userId)
        return object : TokenInterceptorListener {
            override suspend fun onRefreshTokenSuccess(apiToken: ApiToken) {
                setUserToken(user, apiToken)
            }

            override suspend fun onRefreshTokenError() {
                user?.let { onRefreshTokenError?.invoke(it) }
            }

            override suspend fun getApiToken(): ApiToken? {
                user = userDatabase.userDao().findById(userId)
                return user?.apiToken
            }

            override fun getCurrentUserId() = null
        }
    }
    //endregion
}
