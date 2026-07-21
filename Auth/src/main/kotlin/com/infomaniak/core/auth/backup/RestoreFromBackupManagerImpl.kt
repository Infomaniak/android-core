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
@file:OptIn(ExperimentalSplittiesApi::class, ExperimentalSerializationApi::class)

package com.infomaniak.core.auth.backup

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.infomaniak.core.auth.AuthConfiguration.clientId
import com.infomaniak.core.auth.DerivedTokenGenerator
import com.infomaniak.core.auth.DerivedTokenGeneratorImpl
import com.infomaniak.core.auth.api.ApiRoutesCore.TOKEN_URL
import com.infomaniak.core.auth.models.TokenDeviceBinding
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.auth.shouldReport
import com.infomaniak.core.common.Xor
import com.infomaniak.core.common.getAndroidId
import com.infomaniak.core.login.ApiToken
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.sentry.SentryLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.ExperimentalSerializationApi
import splitties.experimental.ExperimentalSplittiesApi

internal class RestoreFromBackupManagerImpl(
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : RestoreFromBackupManager() {

    private val userDb = UserDatabase.instance
    private val userDao = userDb.userDao()

    private val derivedTokenGenerator: DerivedTokenGenerator = DerivedTokenGeneratorImpl(
        coroutineScope = coroutineScope,
        tokenRetrievalUrl = TOKEN_URL,
        clientId = clientId,
        userAgent = HttpUtils.getUserAgent,
    )

    override val state: SharedFlow<State> = flow {
        performRestorationHandlingIfNeeded()
        emit(State.Settled)
    }.distinctUntilChanged().shareIn(coroutineScope, SharingStarted.Eagerly)

    private suspend fun FlowCollector<State>.performRestorationHandlingIfNeeded() {
        val users = userDao.allUsers()
        val currentAndroidId = getAndroidId()
        restoreAccounts(currentAndroidId = currentAndroidId, allUsers = users)
    }

    private tailrec suspend fun FlowCollector<State>.restoreAccounts(
        currentAndroidId: String,
        allUsers: List<User>,
    ) {
        val usersToDeriveTokensFor: List<User> = coroutineScope {
            allUsers.map { user ->
                async {
                    val currentBinding = userDao.getTokenDeviceBindingForUser(user.id)
                    when {
                        currentBinding == null -> {
                            userDao.upsertTokenDeviceBinding(TokenDeviceBinding(user.id, currentAndroidId))
                            null // Adding missing valid binding (post app update).
                        }
                        currentBinding.androidId == currentAndroidId -> null // Already valid.
                        else -> user // Device changed. Need to derive token.
                    }
                }
            }
        }.awaitAll().filterNotNull()

        if (usersToDeriveTokensFor.isEmpty()) return

        emit(State.RestoringFromBackup)

        val issuesWithUser = coroutineScope {
            usersToDeriveTokensFor.map { user ->
                async {
                    when (val result = attemptRestoringAccount(user)) {
                        is Xor.First -> userDb.useWriterConnection {
                            it.immediateTransaction {
                                userDao.update(user.copy(apiToken = result.value))
                                userDao.upsertTokenDeviceBinding(TokenDeviceBinding(user.id, currentAndroidId))
                            }
                            null
                        }
                        is Xor.Second -> result.value to user
                    }
                }
            }
        }.awaitAll().filterNotNull()

        if (issuesWithUser.isEmpty()) return

        val shouldRetryAsync = CompletableDeferred<Boolean>()
        val failedState = State.RestoringFromBackupFailed(
            cause = issuesWithUser.first().first,
            retry = { shouldRetryAsync.complete(true) },
            giveUp = { shouldRetryAsync.complete(false) },
        )
        emit(failedState)
        val shouldRetry = shouldRetryAsync.await()
        val giveUp = !shouldRetry
        if (giveUp) {
            userDb.useWriterConnection {
                it.immediateTransaction {
                    TODO("removeUser from the db, and ensure associated data gets removed too")
                    //TODO: For kDrive, that would be removing:
                    // - MyKSuite data (MyKSuiteDataUtils.deleteData(…))
                    // - everything called in AccountUtils.removeUser
                    // Maybe we need to give the ability to register a callback in the apps,
                    // as well as having a system to put data back in place when the app
                    // process starts with orphan user data (i.e/ user-tied data that is not in the User table)
                }
            }
            return
        }
        restoreAccounts(currentAndroidId = currentAndroidId, allUsers = allUsers)
    }

    private suspend fun attemptRestoringAccount(user: User): Xor<ApiToken, DerivedTokenGenerator.Issue> {
        return derivedTokenGenerator.attemptDerivingOneOfTheseTokens(setOf(user.apiToken.accessToken)).also { result ->
            if (result !is Xor.Second) return@also
            val issue = result.value
            val errorMessage = "Failed to derive token"
            val sentryUser = io.sentry.protocol.User().also { it.id = user.id.toString() }
            if (result.value.shouldReport()) {
                SentryLog.e(TAG, errorMessage, (issue as? DerivedTokenGenerator.Issue.OtherIssue)?.e) { scope ->
                    scope.user = sentryUser
                }
            } else {
                SentryLog.i(TAG, "$errorMessage for user ${user.id}, with reason: $issue")
            }
        }
    }
}

private const val TAG = "RestoreFromBackupManagerImpl"
