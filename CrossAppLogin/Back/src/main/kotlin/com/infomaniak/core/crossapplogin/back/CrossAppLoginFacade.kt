/*
 * Infomaniak Authenticator - Android
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
package com.infomaniak.core.crossapplogin.back

import androidx.activity.ComponentActivity
import com.infomaniak.lib.login.ApiToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface CrossAppLoginFacade {

    companion object {
        fun create(
            applicationId: String,
            clientId: String,
            scope: CoroutineScope,
        ): CrossAppLoginFacade = CrossAppLoginFacadeImpl(applicationId = applicationId, clientId = clientId, scope = scope)

        fun List<ExternalAccount>.filterSelectedAccounts(skippedIds: Set<Long>): List<ExternalAccount> {
            return if (isNotEmpty()) filter { it.id !in skippedIds } else this
        }
    }

    val skippedAccountIds: MutableStateFlow<Set<Long>>

    val accountsCheckingState: StateFlow<AccountsCheckingState>

    suspend fun activateUpdates(
        hostActivity: ComponentActivity,
        singleSelection: Boolean = false
    ): Nothing

    suspend fun attemptLogin(selectedAccounts: List<ExternalAccount>): LoginResult

    @UncheckedTokens
    val availableAccounts: StateFlow<List<ExternalAccount>>

    data class LoginResult(val tokens: List<ApiToken>, val errorMessageIds: List<Int>)

    data class AccountsCheckingState(
        val status: AccountsCheckingStatus,
        val checkedAccounts: List<ExternalAccount> = emptyList(),
    )

    sealed interface AccountsCheckingStatus {
        data object Checking : AccountsCheckingStatus
        data object UpToDate : AccountsCheckingStatus
        sealed class Error : AccountsCheckingStatus {
            data object Network : Error()
            data object Unknown : Error()
        }
    }

    @RequiresOptIn("Tokens in availableAccounts are not checked. Use accountsCheckingState")
    @Retention(AnnotationRetention.BINARY)
    annotation class UncheckedTokens
}
