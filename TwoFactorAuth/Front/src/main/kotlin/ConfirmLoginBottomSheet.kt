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
@file:OptIn(ExperimentalUuidApi::class)

package com.infomaniak.core.twofactorauth.front

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.basics.WithLatestNotNull
import com.infomaniak.core.compose.basics.rememberCallableState
import com.infomaniak.core.twofactorauth.back.AbstractTwoFactorAuthViewModel
import com.infomaniak.core.twofactorauth.front.components.SecurityTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun ConfirmLoginAutoManagedBottomSheet(
    twoFactorAuthViewModel: AbstractTwoFactorAuthViewModel
) = SecurityTheme {
    val challenge by twoFactorAuthViewModel.challengeToResolve.collectAsState()
    ConfirmLoginAutoManagedBottomSheet(challenge)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmLoginAutoManagedBottomSheet(challenge: AbstractTwoFactorAuthViewModel.Challenge?) {

    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showTheSheet by remember { mutableStateOf(false) }

    val confirmRequest = rememberCallableState<Boolean>()

    val challengeAction = challenge?.action
    LaunchedEffect(challengeAction) {
        val action = challengeAction ?: return@LaunchedEffect
        action(confirmRequest.awaitOneCall())
    }
    LaunchedEffect(challenge) {
        when (challenge) {
            null -> {
                sheetState.hideCatching()
                if (sheetState.isVisible.not()) showTheSheet = false
            }
            else -> showTheSheet = true
        }
    }

    if (showTheSheet) challenge?.WithLatestNotNull { challenge ->
        ModalBottomSheet(
            onDismissRequest = { challenge.action?.invoke(null) },
            tonalElevation = 1.dp,
            sheetState = sheetState,
        ) {
            ConfirmLoginBottomSheetContent(
                attemptTimeMark = challenge.attemptTimeMark,
                connectionAttemptInfo = challenge.data,
                confirmRequest = confirmRequest
            )
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
