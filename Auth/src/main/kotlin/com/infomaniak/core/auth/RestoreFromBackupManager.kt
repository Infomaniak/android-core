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
package com.infomaniak.core.auth

import kotlinx.coroutines.flow.SharedFlow

sealed class RestoreFromBackupManager {

    abstract val state: SharedFlow<State>

    abstract suspend fun ensureRestorationIsHandled()

    sealed interface State {

        data object Settled : State

        data object RestoringFromBackup : State

        /**
         * @property giveUp Gives up restoring all accounts that failed, and disconnects them.
         */
        data class RestoringFromBackupFailed(
            val cause: Issue,
            val retry: () -> Unit,
            val giveUp: () -> Unit,
        ) : State {
            sealed interface Issue { // Find a way to have these defined somewhere once and for all.
                data object NetworkIssue : Issue
            }
        }
    }

    companion object {
        val instance: RestoreFromBackupManager = TODO()
    }
}
