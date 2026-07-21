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
package com.infomaniak.core.auth.backup

import com.infomaniak.core.auth.DerivedTokenGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

sealed class RestoreFromBackupManager {

    abstract val state: SharedFlow<State>

    suspend fun ensureRestorationIsHandled() {
        if (State.Settled !in state.replayCache) state.first { it is State.Settled }
    }

    val shouldShowRestorationScreen: Flow<Boolean> = state.map { it != State.Settled }.distinctUntilChanged()

    sealed interface State {

        data object Settled : State

        data object RestoringFromBackup : State

        /**
         * @property giveUp Gives up restoring all accounts that failed, and disconnects them.
         */
        data class RestoringFromBackupFailed(
            val cause: DerivedTokenGenerator.Issue,
            val retry: () -> Unit,
            val giveUp: () -> Unit,
        ) : State
    }

    companion object {
        val instance: RestoreFromBackupManager = RestoreFromBackupManagerImpl()
    }
}
