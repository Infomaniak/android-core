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
@file:OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)

package com.infomaniak.core.twofactorauth.back

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.eventFlow
import androidx.lifecycle.viewModelScope
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.rateLimit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi

abstract class AbstractTwoFactorAuthViewModel : ViewModel() {

    data class Challenge(
        val data: ConnectionAttemptInfo,
        val attemptTimeMark: TimeMark,
        val action: ((Boolean?) -> Unit)?,
    )

    protected abstract suspend fun getConnectedHttpClient(userId: Int): OkHttpClient

    private val rateLimitedForegroundEvents = ProcessLifecycleOwner.get().lifecycle.eventFlow.filter {
        it == Lifecycle.Event.ON_START
    }.rateLimit(30.seconds)

    private val actionedChallengesFlow = MutableStateFlow<List<RemoteChallenge>>(emptyList())

    /**
     * We don't want to bring back any challenges that have been actioned, and are being sent to the backend,
     * or have been fully handled since the most recent call to [attemptGettingAllCurrentChallenges].
     */
    private val firstUnactionedChallenge: Flow<Pair<TwoFactorAuth, RemoteChallenge>?> = rateLimitedForegroundEvents.map {
        attemptGettingAllCurrentChallenges()
    }.flatMapLatest { challengesMap ->
        actionedChallengesFlow.map { actionedChallenges ->
            challengesMap.firstNotNullOfOrNull { (auth, challenge) ->
                challenge.takeIf { it !in actionedChallenges }?.let { auth to challenge }
            }
        }
    }

    val challengeToResolve: StateFlow<Challenge?> = firstUnactionedChallenge.transform { unactionedChallenge ->
        val (twoFactorAuth, remoteChallenge) = unactionedChallenge ?: run {
            emit(null) // Signal that there is no challenge.
            return@transform
        }
        val action = CompletableDeferred<Boolean?>()
        val user = UserDatabase().userDao().findById(twoFactorAuth.userId) ?: run {
            emit(null) // User was removed in the meantime (unlikely, but possible).
            return@transform
        }
        val uiChallenge = Challenge(
            data = remoteChallenge.toConnectionAttemptInfo(user),
            attemptTimeMark = utcTimestampToTimeMark(utcOffsetMillis = remoteChallenge.createdAt * 1000L),
            action = { action.complete(it) }
        )
        emit(uiChallenge)
        val confirmOrReject = action.await()
        emit(uiChallenge.copy(action = null))
        actionedChallengesFlow.update { it + remoteChallenge }
        when (confirmOrReject) {
            true -> twoFactorAuth.approveChallenge(remoteChallenge.uid)
            false -> twoFactorAuth.rejectChallenge(remoteChallenge.uid)
            null -> Unit
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, initialValue = null)

    private val userIds = UserDatabase().userDao().allUsers.map { users ->
        users.mapTo(hashSetOf()) { it.id }
    }.distinctUntilChanged()

    private val allUsersTwoFactorAuth: Flow<List<TwoFactorAuth>> = userIds.mapLatest { userIds ->
        userIds.map { id -> TwoFactorAuthImpl(getConnectedHttpClient(id), id) }
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private suspend fun attemptGettingAllCurrentChallenges(): Map<TwoFactorAuth, RemoteChallenge> = buildMap {
        val currentUsersTwoFactorAuth = allUsersTwoFactorAuth.first()
        currentUsersTwoFactorAuth.forEach { twoFactorAuth ->
            val challenge = twoFactorAuth.tryGettingLatestChallenge() ?: return@forEach
            put(twoFactorAuth, challenge)
        }
    }
}

private fun RemoteChallenge.toConnectionAttemptInfo(user: User): ConnectionAttemptInfo = ConnectionAttemptInfo(
    targetAccount = ConnectionAttemptInfo.TargetAccount(
        avatarUrl = user.avatar,
        fullName = user.displayName ?: user.run { "$firstname $lastname" },
        initials = user.getInitials(),
        email = user.email,
        id = user.id.toLong(),
    ),
    deviceOrBrowserName = device.name,
    deviceType = device.type,
    location = location.name
)

private fun utcTimestampToTimeMark(utcOffsetMillis: Long): TimeMark {
    val nowUtcMillis = System.currentTimeMillis()
    val elapsedMillis = nowUtcMillis - utcOffsetMillis
    return TimeSource.Monotonic.markNow() - elapsedMillis.milliseconds
}
