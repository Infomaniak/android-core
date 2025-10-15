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
@file:OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class, ExperimentalSplittiesApi::class)

package com.infomaniak.core.twofactorauth.back

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.eventFlow
import androidx.lifecycle.viewModelScope
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.rateLimit
import com.infomaniak.core.twofactorauth.back.TwoFactorAuth.Outcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
import splitties.coroutines.raceOf
import splitties.coroutines.repeatWhileActive
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi

abstract class AbstractTwoFactorAuthViewModel : ViewModel() {

    protected abstract suspend fun getConnectedHttpClient(userId: Int): OkHttpClient

    private val rateLimitedForegroundEvents = ProcessLifecycleOwner.get().lifecycle.eventFlow.filter {
        it == Lifecycle.Event.ON_START
    }.rateLimit(30.seconds)

    private val actionedChallengesFlow = MutableStateFlow<Set<RemoteChallenge>>(emptySet())

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
        emit(null) // Start with no challenge.
        val (twoFactorAuth, remoteChallenge) = unactionedChallenge ?: return@transform
        val user = UserDatabase().userDao().findById(twoFactorAuth.userId)
            ?: return@transform // User was removed in the meantime (unlikely, but possible).
        val dismissCompletable = CompletableDeferred<Nothing?>()
        val confirmOrRejectAsync = CompletableDeferred<Boolean>()
        val uiChallenge = Challenge(
            data = remoteChallenge.toConnectionAttemptInfo(user),
            attemptTimeMark = utcTimestampToTimeMark(utcOffsetMillis = remoteChallenge.createdAt * 1000L),
            dismiss = { dismissCompletable.complete(null) },
            state = Challenge.State.ApproveOrReject(action = { confirmOrRejectAsync.complete(it) })
        )
        repeatWhileActive {
            emit(uiChallenge)
            val confirmOrReject: Boolean? = raceOf(
                { confirmOrRejectAsync.await() },
                { dismissCompletable.await() }
            )
            emit(uiChallenge.copy(state = null))
            actionedChallengesFlow.update { it + remoteChallenge }
            // We intentionally don't immediately act on dismissal while sending the action to the backend.
            val outcome: Outcome = when (confirmOrReject) {
                true -> twoFactorAuth.approveChallenge(remoteChallenge.uuid)
                false -> twoFactorAuth.rejectChallenge(remoteChallenge.uuid)
                null -> return@transform // Dismissal.
            }
            when (outcome) {
                is Outcome.Done -> if (confirmOrReject) { // Confirmed.
                    emit(uiChallenge.copy(state = Challenge.State.Done(outcome)))
                    when (outcome) {
                        Outcome.Done.Success -> Unit
                        Outcome.Done.Rejected, Outcome.Done.Expired, Outcome.Done.AlreadyProcessed -> dismissCompletable.join()
                    }
                    return@transform
                } else { // Rejected.
                    emit(uiChallenge.copy(state = Challenge.State.Done(Outcome.Done.Rejected)))
                    dismissCompletable.join()
                    return@transform
                }
                is Outcome.Issue -> {
                    val retryCompletable = Job()
                    emit(uiChallenge.copy(state = Challenge.State.Issue(outcome, retry = { retryCompletable.complete() })))
                    raceOf(
                        { retryCompletable.join() },
                        { dismissCompletable.await() }
                    ) ?: return@transform // Break the retry loop as dismissal hands back `null`.
                }
            }
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

    data class Challenge(
        val data: ConnectionAttemptInfo,
        val attemptTimeMark: TimeMark,
        val dismiss: () -> Unit,
        val state: State?
    ) {
        sealed interface State {

            /** @property action Send true for approval, false for reject, and null for dismiss */
            data class ApproveOrReject(val action: ((Boolean) -> Unit)) : State
            data class Done(val data: Outcome.Done) : State
            data class Issue(
                val data: Outcome.Issue,
                val retry: () -> Unit
            ) : State
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
    location = location
)

private fun utcTimestampToTimeMark(utcOffsetMillis: Long): TimeMark {
    val nowUtcMillis = System.currentTimeMillis()
    val elapsedMillis = nowUtcMillis - utcOffsetMillis
    return TimeSource.Monotonic.markNow() - elapsedMillis.milliseconds
}
