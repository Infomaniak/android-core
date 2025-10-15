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
import com.infomaniak.core.twofactorauth.back.AbstractTwoFactorAuthViewModel.Challenge.State.Done
import com.infomaniak.core.twofactorauth.back.TwoFactorAuth.Outcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
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

        // User was removed in the meantime (unlikely, but possible).
        val user = UserDatabase().userDao().findById(twoFactorAuth.userId) ?: return@transform

        val dismissCompletable = CompletableDeferred<Nothing?>()
        val confirmOrRejectAsync = CompletableDeferred<Challenge.ApprovalAction>()
        val uiChallenge = Challenge(
            data = remoteChallenge.toConnectionAttemptInfo(user),
            attemptTimeMark = utcTimestampToTimeMark(utcOffsetMillis = remoteChallenge.createdAt * 1000L),
            dismiss = { dismissCompletable.complete(null) },
            state = Challenge.State.ApproveOrReject(action = { confirmOrRejectAsync.complete(it) })
        )
        val awaitForUserChoice: suspend () -> Challenge.ApprovalAction? = {
            raceOf(
                { confirmOrRejectAsync.await() },
                { dismissCompletable.await() }
            )
        }

        repeatWhileActive {
            emit(uiChallenge)
            val userChoice = awaitForUserChoice()

            emit(uiChallenge.copy(state = null))
            actionedChallengesFlow.update { it + remoteChallenge }

            // We intentionally don't immediately act on dismissal while sending the action to the backend.
            val outcome: Outcome = when (userChoice) {
                Challenge.ApprovalAction.Approve -> twoFactorAuth.approveChallenge(remoteChallenge.uuid)
                Challenge.ApprovalAction.Reject -> twoFactorAuth.rejectChallenge(remoteChallenge.uuid)
                null -> return@transform // Dismissal.
            }

            if (handleOutcome(outcome, userChoice, uiChallenge, dismissCompletable)) return@transform
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, initialValue = null)

    /**
     * Emit a new version of the [AbstractTwoFactorAuthViewModel.Challenge] with its state updated accordingly to the [Outcome]
     *
     * @return true if the user approved, denied or dismissed the two factor authorization or false if an error occurred
     * and the user clicked on `Retry`
     */
    private suspend fun FlowCollector<Challenge?>.handleOutcome(
        outcome: Outcome,
        userChoice: Challenge.ApprovalAction,
        uiChallenge: Challenge,
        dismissCompletable: CompletableDeferred<Nothing?>,
    ): Boolean = when (outcome) {
        is Outcome.Done -> {
            val doneChallengeState = computeDoneChallengeState(userChoice, outcome)
            emit(uiChallenge.copy(state = doneChallengeState))
            if (doneChallengeState.data != Outcome.Done.Success) dismissCompletable.join()
            true // Return true to break repeatWhileActive loop as the challenge was either approved or dismissed
        }
        is Outcome.Issue -> {
            val retryCompletable = Job()
            val issueChallengeState = Challenge.State.Issue(data = outcome, retry = { retryCompletable.complete() })
            emit(uiChallenge.copy(state = issueChallengeState))
            // Wait for either user choose to retry or dismiss the screen
            val retryOrDismissResult = raceOf({ retryCompletable.join() }, { dismissCompletable.await() })

            // Break the retry loop by returning true as dismissal completes with `null`, which means user dismissed the screen
            // Or return false to continue in repeatWhileActive loop if retry completed
            retryOrDismissResult == null
        }
    }

    private fun computeDoneChallengeState(userChoice: Challenge.ApprovalAction, outcome: Outcome.Done): Done {
        return if (userChoice == Challenge.ApprovalAction.Approve && outcome == Outcome.Done.Success) {
            Done(Outcome.Done.Success)
        } else {
            val unsuccessfulOutcome = if (userChoice == Challenge.ApprovalAction.Approve) outcome else Outcome.Done.Rejected
            Done(unsuccessfulOutcome)
        }
    }

    private val userIds = UserDatabase().userDao().allUsers.map { users ->
        users.mapTo(hashSetOf()) { it.id }
    }.distinctUntilChanged()

    private val allUsersTwoFactorAuth: Flow<List<TwoFactorAuth>> = userIds.mapLatest { userIds ->
        userIds.map { id -> TwoFactorAuthImpl(getConnectedHttpClient(id), id) }
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private suspend fun attemptGettingAllCurrentChallenges(): Map<TwoFactorAuth, RemoteChallenge> = flow {
        val currentUsersTwoFactorAuth = allUsersTwoFactorAuth.first()
        currentUsersTwoFactorAuth.forEach { twoFactorAuth ->
            val challenge = twoFactorAuth.tryGettingLatestChallenge() ?: return@forEach
            emit(twoFactorAuth to challenge)
        }
    }.toList().sortedBy { (_, challenge) ->
        challenge.createdAt
    }.toMap()

    data class Challenge(
        val data: ConnectionAttemptInfo,
        val attemptTimeMark: TimeMark,
        val dismiss: () -> Unit,
        val state: State?
    ) {
        enum class ApprovalAction { Approve, Reject; }

        sealed interface State {

            /** @property action Send true for approval, false for reject, and null for dismiss */
            data class ApproveOrReject(val action: ((ApprovalAction) -> Unit)) : State
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
