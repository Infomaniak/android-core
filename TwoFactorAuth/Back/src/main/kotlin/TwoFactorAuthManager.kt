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
import androidx.lifecycle.eventFlow
import com.infomaniak.core.Xor
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.emitFirstsUntilSecond
import com.infomaniak.core.rateLimit
import com.infomaniak.core.twofactorauth.back.TwoFactorAuth.Outcome
import com.infomaniak.core.twofactorauth.back.TwoFactorAuthManager.Challenge
import com.infomaniak.core.twofactorauth.back.TwoFactorAuthManager.Challenge.State.Done
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.flow.transformLatest
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

/**
 * All consumers need is [challengeToResolve].
 *
 * The state inside the [Challenge] type contains callbacks to perform the relevant actions.
 *
 * ### IMPORTANT:
 *
 * The instance of this class **shall be a singleton**.
 *
 * Initialize it in a top-level property, or inside another singleton (not in a getter!):
 * ```
 * val twoFactorAuthManager = TwoFactorAuthManager { userId -> AccountUtils.getHttpClient(userId) }
 * ```
 *
 * This will ensure that there's no double concurrent states.
 */
class TwoFactorAuthManager(
    // The CoroutineScope (and its Job) is strongly referenced by shareIn and stateIn, so no need to keep a reference here.
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val getConnectedHttpClient: suspend (userId: Int) -> OkHttpClient
) {

    private val rateLimitedForegroundEvents = ProcessLifecycleOwner.get().lifecycle.eventFlow.filter {
        it == Lifecycle.Event.ON_START
    }.rateLimit(30.seconds, elementsBeforeLimit = 2)

    private val actionedChallengesFlow = MutableStateFlow<Set<RemoteChallenge>>(emptySet())

    /**
     * We don't want to bring back any challenges that have been actioned, and are being sent to the backend,
     * or have been fully handled since the most recent call to [attemptGettingAllCurrentChallenges].
     */
    private val firstUnactionedChallenge: StateFlow<Pair<TwoFactorAuth, RemoteChallenge>?> = rateLimitedForegroundEvents.map {
        attemptGettingAllCurrentChallenges()
    }.flatMapLatest { challengesMap ->
        actionedChallengesFlow.map { actionedChallenges ->
            challengesMap.firstNotNullOfOrNull { (auth, challenge) ->
                challenge.takeIf { it !in actionedChallenges }?.let { auth to challenge }
            }
        }
    }.distinctUntilChanged().stateIn(
        scope = coroutineScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    val challengeToResolve: StateFlow<Challenge?> = flow {
        repeatWhileActive {
            val actionData = emitOngoingChallengeUntilAction(firstUnactionedChallenge = firstUnactionedChallenge)
            actionedChallengesFlow.update { it + actionData.remoteChallenge }
            executeChallengeAction(actionData)
        }
    }.stateIn(coroutineScope, SharingStarted.Lazily, initialValue = null)

    private val userIds = UserDatabase().userDao().allUsers.map { users ->
        users.mapTo(hashSetOf()) { it.id }
    }.distinctUntilChanged()

    private val allUsersTwoFactorAuth: Flow<List<TwoFactorAuth>> = userIds.mapLatest { userIds ->
        userIds.map { id -> TwoFactorAuthImpl(getConnectedHttpClient(id), id) }
    }.shareIn(coroutineScope, SharingStarted.Lazily, replay = 1)

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
        val state: State
    ) {
        enum class ApprovalAction { Approve, Reject; }

        sealed interface State {

            /** @property action Send true for approval, false for reject, and null for dismiss */
            data class ApproveOrReject(val action: ((ApprovalAction) -> Unit)) : Ongoing
            data class SendingAction(val userChoice: ApprovalAction) : Ongoing
            data class Done(val data: Outcome.Done) : State
            data class Issue(val data: Outcome.Issue, val retry: () -> Unit) : State

            sealed interface Ongoing : State
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

/**
 * Emits the ongoing challenge (or null if none yet), and returns the [ActionedChallengeData] once it has been… actioned
 * (by the user through the callback in the state included into the [Challenge] object that was emitted beforehand).
 */
private suspend fun FlowCollector<Challenge?>.emitOngoingChallengeUntilAction(
    firstUnactionedChallenge: Flow<Pair<TwoFactorAuth, RemoteChallenge>?>,
): ActionedChallengeData {
    return emitFirstsUntilSecond(ongoingChallengeAndActionFlow(firstUnactionedChallenge))
}

private fun ongoingChallengeAndActionFlow(
    firstUnactionedChallenge: Flow<Pair<TwoFactorAuth, RemoteChallenge>?>,
): Flow<Xor<Challenge?, ActionedChallengeData>> = firstUnactionedChallenge.transformLatest { unactionedChallenge ->
    emit(Xor.First(null)) // Start with no challenge.
    val (twoFactorAuth, remoteChallenge) = unactionedChallenge ?: return@transformLatest
    val user = UserDatabase().userDao().findById(twoFactorAuth.userId)
        ?: return@transformLatest // User was removed in the meantime (unlikely, but possible).

    val dismissCompletable = CompletableDeferred<Nothing?>()
    val confirmOrRejectAsync = CompletableDeferred<Challenge.ApprovalAction>()
    val uiChallenge = Challenge(
        data = remoteChallenge.toConnectionAttemptInfo(user),
        attemptTimeMark = utcTimestampToTimeMark(utcOffsetMillis = remoteChallenge.createdAt * 1000L),
        dismiss = { dismissCompletable.complete(null) },
        state = Challenge.State.ApproveOrReject(action = { confirmOrRejectAsync.complete(it) })
    )
    emit(Xor.First(uiChallenge))
    val userChoice = raceOf(
        { confirmOrRejectAsync.await() },
        { dismissCompletable.await() }
    )
    val actionedChallengeData = ActionedChallengeData(
        remoteChallenge = remoteChallenge,
        uiChallenge = uiChallenge,
        dismissCompletable = dismissCompletable,
        twoFactorAuth = twoFactorAuth,
        userChoice = userChoice
    )
    emit(Xor.Second(actionedChallengeData))
}

/**
 * Performs the backend API call according to the user choice, and handles potential issues (with retry ability).
 */
private suspend fun FlowCollector<Challenge?>.executeChallengeAction(actionData: ActionedChallengeData) {

    val userChoice = actionData.userChoice ?: return // Dismissal, nothing to do.

    val dismissCompletable = actionData.dismissCompletable
    val uiChallenge = actionData.uiChallenge.copy(state = Challenge.State.SendingAction(userChoice = userChoice))
    val challengeUid = actionData.remoteChallenge.uuid
    val twoFactorAuth = actionData.twoFactorAuth

    repeatWhileActive {
        emit(uiChallenge)
        // We intentionally don't immediately act on dismissal while sending the action to the backend.
        val outcome: Outcome = when (userChoice) {
            Challenge.ApprovalAction.Approve -> twoFactorAuth.approveChallenge(challengeUid)
            Challenge.ApprovalAction.Reject -> twoFactorAuth.rejectChallenge(challengeUid)
        }
        if (handleOutcome(outcome, userChoice, uiChallenge, dismissCompletable)) return
    }
}

/**
 * Emit a new version of the [TwoFactorAuthManager.Challenge] with its state updated accordingly to the [Outcome],
 * and let the user dismiss or ask retrying if applicable.
 *
 * @return `true` if handling is done (e.g. the user approved, denied, or dismissed the 2FA challenge),
 * `false` if there's an error and the user chose to retry.
 */
private suspend fun FlowCollector<Challenge?>.handleOutcome(
    outcome: Outcome,
    userChoice: Challenge.ApprovalAction,
    uiChallenge: Challenge,
    dismissCompletable: CompletableDeferred<Nothing?>,
): Boolean = when (outcome) {
    is Outcome.Done -> {
        val doneChallengeState = outcome.toDoneChallengeState(userChoice)
        emit(uiChallenge.copy(state = doneChallengeState))
        if (doneChallengeState.data != Outcome.Done.Success) dismissCompletable.join()
        true // Return true to break the loop as the challenge was either approved or dismissed
    }
    is Outcome.Issue -> {
        val retryCompletable = Job()
        val issueChallengeState = Challenge.State.Issue(data = outcome, retry = { retryCompletable.complete() })
        emit(uiChallenge.copy(state = issueChallengeState))
        // Wait for either user to choose between retry or dismissal
        val wasDismissed = raceOf({ retryCompletable.join() }, { dismissCompletable.await() }) == null

        // If dismissed, we consider handling done, hence returning `true`.
        // Otherwise, we return `false` to have the caller loop for the retry action.
        wasDismissed
    }
}

private fun Outcome.Done.toDoneChallengeState(
    userChoice: Challenge.ApprovalAction
): Done = when (this) {
    Outcome.Done.Success -> Done(if (userChoice == Challenge.ApprovalAction.Reject) Outcome.Done.Rejected else this)
    else -> Done(this)
}

private data class ActionedChallengeData(
    val remoteChallenge: RemoteChallenge,
    val uiChallenge: Challenge,
    val dismissCompletable: CompletableDeferred<Nothing?>,
    val twoFactorAuth: TwoFactorAuth,
    val userChoice: Challenge.ApprovalAction?,
)
