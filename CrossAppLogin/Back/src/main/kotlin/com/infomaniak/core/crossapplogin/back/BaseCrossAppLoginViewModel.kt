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
package com.infomaniak.core.crossapplogin.back

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.infomaniak.core.Xor
import com.infomaniak.core.crossapplogin.back.DerivedTokenGenerator.Issue
import com.infomaniak.core.network.LOGIN_ENDPOINT_URL
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.network.utils.bodyAsStringOrNull
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.lib.login.ApiToken
import io.sentry.IScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.ExperimentalSerializationApi
import com.infomaniak.core.R as RCore
import com.infomaniak.core.appintegrity.R as RAppIntegrity
import com.infomaniak.core.network.R as RCoreNetwork

abstract class BaseCrossAppLoginViewModel(applicationId: String, clientId: String) : ViewModel() {
    private val _availableAccounts = MutableStateFlow(emptyList<ExternalAccount>())
    val availableAccounts: StateFlow<List<ExternalAccount>> = _availableAccounts.asStateFlow()
    val skippedAccountIds = MutableStateFlow(emptySet<Long>())

    // TODO: Remove once mail uses the compose onboarding screen as well. This value won't be needed anymore then.
    val selectedAccounts: StateFlow<List<ExternalAccount>> =
        combine(availableAccounts, skippedAccountIds) { allExternalAccounts, idsToSkip ->
            allExternalAccounts.filterSelectedAccounts(idsToSkip)
        }.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    private val derivedTokenGenerator: DerivedTokenGenerator = DerivedTokenGeneratorImpl(
        coroutineScope = viewModelScope,
        tokenRetrievalUrl = "$LOGIN_ENDPOINT_URL/token",
        hostAppPackageName = applicationId,
        clientId = clientId,
        userAgent = HttpUtils.getUserAgent,
    )

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun activateUpdates(hostActivity: ComponentActivity, singleSelection: Boolean = false): Nothing = coroutineScope {
        if (!derivedTokenGenerator.isAppIntegrityCallable()) throw CancellationException()

        val crossAppLogin = CrossAppLogin.forContext(
            context = hostActivity,
            coroutineScope = this + Dispatchers.Default
        )
        if (singleSelection) launch { keepSingleSelection() }
        hostActivity.repeatOnLifecycle(Lifecycle.State.STARTED) {
            _availableAccounts.emit(crossAppLogin.retrieveAccountsFromOtherApps())
        }
        awaitCancellation() // Should never be reached. Unfortunately, `repeatOnLifecycle` doesn't return `Nothing`.
    }

    suspend fun attemptLogin(selectedAccounts: List<ExternalAccount>): LoginResult {
        val tokenGenerator = derivedTokenGenerator

        val tokens = mutableListOf<ApiToken>()
        val errorMessageIds = mutableListOf<Int>()

        selectedAccounts.forEach { account ->
            when (val result = tokenGenerator.attemptDerivingOneOfTheseTokens(account.tokens)) {
                is Xor.First -> {
                    SentryLog.i(TAG, "Succeeded to derive token for account: ${account.id}")
                    tokens.add(result.value)
                }
                is Xor.Second -> {
                    errorMessageIds.add(getTokenDerivationIssueErrorMessage(account, issue = result.value))
                }
            }
        }

        return LoginResult(tokens, errorMessageIds)
    }

    /**
     * Ensures that there's only one selected account, even through updates to [availableAccounts] and [skippedAccountIds].
     */
    private suspend fun keepSingleSelection(): Nothing {
        availableAccounts.collectLatest { accounts ->
            skippedAccountIds.collectLatest { currentSkippedAccountIds ->
                skippedAccountIds.value = newSkippedAccountIdsToKeepSingleSelection(accounts, currentSkippedAccountIds)
            }
        }
        awaitCancellation() // Unreachable because availableAccounts is a StateFlow, and collectLatest is not truncating.
    }

    // @StringRes doesn't work with a suspend function because they technically return java.lang.Object
    private suspend fun getTokenDerivationIssueErrorMessage(account: ExternalAccount, issue: Issue): Int {
        val shouldReport: Boolean
        val messageResId = when (issue) {
            is Issue.AppIntegrityCheckFailed -> {
                shouldReport = false
                RAppIntegrity.string.errorAppIntegrity
            }
            is Issue.ErrorResponse -> {
                shouldReport = issue.response.code !in 500..599
                RCore.string.anErrorHasOccurred
            }
            is Issue.NetworkIssue -> {
                shouldReport = false
                RCoreNetwork.string.connectionError
            }
            is Issue.OtherIssue -> {
                shouldReport = true
                RCore.string.anErrorHasOccurred
            }
        }

        val details = when (issue) {
            is Issue.ErrorResponse -> runCatching { issue.response.bodyAsStringOrNull() ?: "" }.getOrDefault("")
            else -> ""
        }
        val errorMessage = "Failed to derive token"
        when (shouldReport) {
            true -> SentryLog.e(TAG, errorMessage, (issue as? Issue.OtherIssue)?.e) { scope ->
                scope.addErrorExtraAndTag(account, issue, details)
            }
            false -> SentryLog.i(TAG, "$errorMessage for account ${account.id}, with reason: $issue | Details: [$details]")
        }

        return messageResId
    }

    private fun IScope.addErrorExtraAndTag(account: ExternalAccount, issue: Issue, details: String) {
        setTag("accountId", account.id.toString())
        setExtra("accountId", account.id.toString())
        setExtra("issue", issue.toString())
        setExtra("details", details)
    }

    data class LoginResult(val tokens: List<ApiToken>, val errorMessageIds: List<Int>)

    companion object {
        private val TAG = BaseCrossAppLoginViewModel::class.java.simpleName

        fun List<ExternalAccount>.filterSelectedAccounts(skippedIds: Set<Long>): List<ExternalAccount> {
            return if (isNotEmpty()) filter { it.id !in skippedIds } else this
        }
    }
}
