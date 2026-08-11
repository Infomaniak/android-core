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
import kotlinx.coroutines.flow.map
import splitties.init.appCtx

sealed class RestoreFromBackupManager {

    abstract val state: SharedFlow<State>

    abstract suspend fun waitForRestorationCompletion(targetUserId: Int?)

    val shouldShowRestorationScreen: Flow<Boolean> by lazy { state.map { it != State.Settled }.distinctUntilChanged() }

    abstract fun registerRemoveUser(removeUser: suspend (id: Int) -> Unit)

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

    enum class RestorationMode {
        /** Handled by [RestoreFromBackupManager], with token derivation. */
        TokenDerivation,
        /** Handled externally (with passkeys). */
        External,
    }

    companion object {
        val instance: RestoreFromBackupManager = RestoreFromBackupManagerImpl(
            mode = if ("com.infomaniak.auth".let { packageWithPasskey ->
                val currentAppId = appCtx.packageName
                currentAppId == packageWithPasskey || currentAppId.startsWith("$packageWithPasskey.")
            }) {
                RestorationMode.External
            } else {
                RestorationMode.TokenDerivation
            }
        )
    }
}
