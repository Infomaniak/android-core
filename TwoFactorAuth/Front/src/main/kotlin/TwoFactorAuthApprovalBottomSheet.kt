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
@file:OptIn(ExperimentalUuidApi::class, ExperimentalSplittiesApi::class)

package com.infomaniak.core.twofactorauth.front

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.basics.CallableState
import com.infomaniak.core.compose.basics.WithLatestNotNull
import com.infomaniak.core.twofactorauth.back.AbstractTwoFactorAuthViewModel
import com.infomaniak.core.twofactorauth.back.AbstractTwoFactorAuthViewModel.Challenge
import com.infomaniak.core.twofactorauth.back.TwoFactorAuth.Outcome
import com.infomaniak.core.twofactorauth.front.components.SecurityTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun TwoFactorAuthApprovalAutoManagedBottomSheet(
    twoFactorAuthViewModel: AbstractTwoFactorAuthViewModel
) = SecurityTheme {
    val challengeState = twoFactorAuthViewModel.challengeToResolve.collectAsState()
    TwoFactorAuthApprovalAutoManagedBottomSheet(challengeState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoFactorAuthApprovalAutoManagedBottomSheet(challengeState: State<Challenge?>) {
    // Must be a state read rather than a simple parameter, to ensure that the confirmValueChange lambda
    // below (remembered by default) can read an up to date value (and not the one from the first composition).
    val challenge: Challenge? by challengeState

    val sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { if (it == SheetValue.Hidden) challenge == null else true }
    )
    var showTheSheet by remember { mutableStateOf(false) }

    LaunchedEffect(challenge) {
        when (challenge) {
            null -> {
                sheetState.hideCatching()
                if (sheetState.isVisible.not()) showTheSheet = false
            }
            else -> showTheSheet = true
        }
    }

    if (showTheSheet) challenge.WithLatestNotNull { challenge ->
        ModalBottomSheet(
            onDismissRequest = challenge.dismiss,
            dragHandle = null,
            tonalElevation = 1.dp,
            contentWindowInsets = { WindowInsets.systemBars },
            sheetState = sheetState,
        ) {
            TwoFactorAuthApprovalContent(challenge)
        }
    }
}

@ExperimentalMaterial3Api
private suspend fun SheetState.hideCatching(): Boolean = try {
    hide()
    true
} catch (_: CancellationException) { // Thrown when the user swipes the sheet during hiding.
    currentCoroutineContext().ensureActive()
    false
}

@Preview
@Composable
private fun TwoFactorAuthApprovalAutoManagedBottomSheetPreview() = SecurityTheme {
    val state = produceState<Challenge?>(initialValue = null) {
        var iteration = 0
        while (true) {
            iteration++
            val approval = CallableState<Challenge.ApprovalAction>()
            val dismissal = Job()
            val dismiss = fun () { dismissal.complete() }
            value = challengeForPreview(Challenge.State.ApproveOrReject(approval), dismiss = dismiss)
            raceOf({ dismissal.join() }, {
                val rejected = approval.awaitOneCall() == Challenge.ApprovalAction.Reject
                value = challengeForPreview(state = null, dismiss = dismiss)
                delay(.5.seconds)
                val outcome = when (iteration % 3) {
                    1 -> Outcome.Done.AlreadyProcessed
                    2 -> Outcome.Done.Expired
                    else -> if (rejected) Outcome.Done.Rejected else null
                }
                outcome?.let {
                    value = challengeForPreview(state = Challenge.State.Done(it), dismiss = dismiss)
                    awaitCancellation()
                }
            })
            value = null
            delay(2.seconds)
        }
    }
    // We have a black background to hide the ugly-ish PreviewActivity that doesn't support edge-to-edge.
    //TODO[issue-blocked]: Remove when https://issuetracker.google.com/issues/441665274 is addressed.
    Box(Modifier.background(Color.Black).fillMaxSize()) {
        TwoFactorAuthApprovalAutoManagedBottomSheet(state)
    }
}
