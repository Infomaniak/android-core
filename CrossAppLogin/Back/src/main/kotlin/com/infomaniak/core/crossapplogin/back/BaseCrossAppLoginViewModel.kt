/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.core.DynamicLazyMap
import com.infomaniak.core.Xor
import com.infomaniak.core.auth.api.ApiRepositoryCore
import com.infomaniak.core.auth.api.ApiRoutesCore.TOKEN_URL
import com.infomaniak.core.cancellable
import com.infomaniak.core.completableScope
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel.AccountsCheckingStatus.*
import com.infomaniak.core.crossapplogin.back.DerivedTokenGenerator.Issue
import com.infomaniak.core.crossapplogin.back.internal.CustomTokenInterceptor
import com.infomaniak.core.mapSync
import com.infomaniak.core.network.models.exceptions.NetworkException
import com.infomaniak.core.network.networking.HttpClient.addCache
import com.infomaniak.core.network.networking.HttpClient.addCommonInterceptors
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.network.utils.bodyAsStringOrNull
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.lib.login.ApiToken
import io.sentry.IScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.seconds
import com.infomaniak.core.R as RCore
import com.infomaniak.core.network.R as RCoreNetwork

abstract class BaseCrossAppLoginViewModel(applicationId: String, clientId: String) : ViewModel() {

    private val _availableAccounts = MutableStateFlow<CrossAppLogin.AccountsFromOtherApps?>(null)
    val availableAccounts: StateFlow<List<ExternalAccount>> = _availableAccounts.asStateFlow().mapSync {
        it?.accounts ?: emptyList()
    }

    val skippedAccountIds = MutableStateFlow(emptySet<Long>())

    val accountsCheckingState: StateFlow<AccountsCheckingState> = accountsCheckingStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = AccountsCheckingState(status = Checking)
    )

    private val derivedTokenGenerator: DerivedTokenGenerator = DerivedTokenGeneratorImpl(
        coroutineScope = viewModelScope,
        tokenRetrievalUrl = TOKEN_URL,
        hostAppPackageName = applicationId,
        clientId = clientId,
        userAgent = HttpUtils.getUserAgent,
    )

    private val accountCheckStatuses = DynamicLazyMap<ExternalAccount, _>(
        cacheManager = { _, _ -> awaitCancellation() },
        coroutineScope = viewModelScope,
        createElement = { account -> async { getFirstValidTokenOrError(account) } },
    )

    private val baseOkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addCache()
            addCommonInterceptors()
        }
    }

    private val isCrossAppLoginEnabledFlow = MutableStateFlow(true)

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun activateUpdates(hostActivity: ComponentActivity, singleSelection: Boolean = false): Nothing = coroutineScope {
        // Do nothing if the user's device cannot be verified via Play's AppIntegrity, to avoid displaying the CrossAppLogin
        if (!derivedTokenGenerator.checkIfAppIntegrityCouldSucceed()) {
            _availableAccounts.emit(CrossAppLogin.AccountsFromOtherApps(emptyList()))
            awaitCancellation()
        }

        accountsCheckingState.launchIn(this) // Make the flow hot.
        // We don't use SharingStarted.Eagerly because the ViewModel can be present without
        // needing the StateFlow to be hot (e.g. in a single Activity app)

        val crossAppLogin = CrossAppLogin.forContext(
            context = hostActivity,
            coroutineScope = this + Dispatchers.Default
        )
        isCrossAppLoginEnabledFlow.collectLatest { isEnabled ->
            if (!isEnabled) {
                _availableAccounts.value = CrossAppLogin.AccountsFromOtherApps(emptyList())
                return@collectLatest
            }
            if (singleSelection) launch { keepSingleSelection() }
            _availableAccounts.emitAll(crossAppLogin.accountsFromOtherApps(hostActivity.lifecycle))
        }
        awaitCancellation() // Can't be reached because isCrossAppLoginEnabledFlow is a SharedFlow.
    }

    suspend fun attemptLogin(selectedAccounts: List<ExternalAccount>): LoginResult {
        val tokenGenerator = derivedTokenGenerator

        val tokens = mutableListOf<ApiToken>()
        val errorMessageIds = mutableListOf<Int>()
        var hadATerminalIssue = false

        selectedAccounts.forEach { account ->
            when (val result = tokenGenerator.attemptDerivingOneOfTheseTokens(account.tokens)) {
                is Xor.First -> {
                    SentryLog.i(TAG, "Succeeded to derive token for account: ${account.id}")
                    tokens.add(result.value)
                }
                is Xor.Second -> {
                    hadATerminalIssue = hadATerminalIssue || result.value !is Issue.NetworkIssue
                    errorMessageIds.add(getTokenDerivationIssueErrorMessage(account, issue = result.value))
                }
            }
        }

        val shouldDisableCrossAppLogin = hadATerminalIssue && tokens.isEmpty()
        if (shouldDisableCrossAppLogin) {
            isCrossAppLoginEnabledFlow.value = false
        }

        return LoginResult(tokens, errorMessageIds)
    }

    private fun accountsCheckingStateFlow(): Flow<AccountsCheckingState> = channelFlow {

        val stateFlow = MutableStateFlow(AccountsCheckingState(status = Checking))
        send(stateFlow.value)

        _availableAccounts.collectLatest { accountsFromOtherApps ->
            val accounts = accountsFromOtherApps?.accounts
            when {
                accounts == null -> return@collectLatest send(AccountsCheckingState(status = Checking))
                accounts.isEmpty() -> {
                    val allAppsChecked = accountsFromOtherApps.allAppsChecked
                    if (!allAppsChecked) {
                        delay(2.seconds) // Give 2 more seconds for another app to possibly return accounts
                    }
                    return@collectLatest send(AccountsCheckingState(status = UpToDate))
                }
            }

            val errorIfAny = MutableStateFlow<Error?>(null)
            val currentlyCheckedAccounts = MutableStateFlow(emptyList<ExternalAccount>())

            send(stateFlow.value.copy(status = Checking))

            coroutineScope {
                accounts.forEach { account ->
                    launch {
                        when (val result = checkAccount(account)) {
                            is AccountCheckResult.Valid -> {
                                currentlyCheckedAccounts.update { it + result.account }
                                send(stateFlow.updateAndGet { it.withAccount(result.account) })
                            }
                            AccountCheckResult.Issue.Network -> errorIfAny.update { it ?: Error.Network }
                            AccountCheckResult.Issue.Other -> errorIfAny.update { it }
                        }
                    }
                }
            }

            val finalValue = stateFlow.updateAndGet { lastState ->
                val accountsToKeep = currentlyCheckedAccounts.value // We will filter accounts that are no longer in other apps.
                lastState.copy(
                    status = errorIfAny.value ?: UpToDate,
                    checkedAccounts = lastState.checkedAccounts.filter { it in accountsToKeep }
                )
            }
            send(finalValue)
        }
    }.conflate()

    private fun AccountsCheckingState.withAccount(account: ExternalAccount): AccountsCheckingState {
        return copy(checkedAccounts = (checkedAccounts + account).distinctBy { it.email })
    }

    private suspend fun checkAccount(account: ExternalAccount): AccountCheckResult {
        return accountCheckStatuses.useElement(account) { resultAsync -> resultAsync.await() }
    }

    private suspend fun checkToken(account: ExternalAccount, token: String): AccountCheckResult = runCatching {

        val customTokenHttpClient = baseOkHttpClient.addInterceptor(CustomTokenInterceptor(token)).build()

        if (ApiRepositoryCore.checkTokenValidity(customTokenHttpClient)) {
            // Put the just checked token first. Will speed up later derivation attempts.
            val tokens = setOf(token) + account.tokens
            AccountCheckResult.Valid(account.copy(tokens = tokens))
        } else {
            AccountCheckResult.Issue.Other
        }
    }.cancellable().getOrElse { exception ->
        when (exception) {
            is NetworkException -> AccountCheckResult.Issue.Network
            else -> AccountCheckResult.Issue.Other
        }
    }

    /**
     * Use a [completableScope] to check all token concurrently, and stop as soon as one of them returned a valid data
     *
     * @return The [AccountsCheckingStatus] of the check: [Checking] if it succeeded, [Error] otherwise
     */
    private suspend fun getFirstValidTokenOrError(
        account: ExternalAccount
    ): AccountCheckResult = completableScope { completable ->
        val asyncTokenChecks = account.tokens.map { token -> async { checkToken(account, token) } }
        // Default result. Any other issue is more important.
        var mostImportantIssue: AccountCheckResult.Issue = AccountCheckResult.Issue.Network

        asyncTokenChecks.forEach { tokenCheck ->
            launch {
                when (val result = tokenCheck.await()) {
                    is AccountCheckResult.Valid -> completable.complete(result)
                    AccountCheckResult.Issue.Network -> Unit
                    is AccountCheckResult.Issue -> mostImportantIssue = result // The last non-network error "wins".
                }
            }
        }

        mostImportantIssue // Will make it only if there is no match after all child coroutines complete.
    }

    /**
     * Ensures that there's only one selected account, even through updates to [accountsCheckingState] and [skippedAccountIds].
     */
    private suspend fun keepSingleSelection(): Nothing {
        accountsCheckingState.collectLatest { state ->
            skippedAccountIds.collectLatest { currentSkippedAccountIds ->
                skippedAccountIds.value = newSkippedAccountIdsToKeepSingleSelection(
                    accounts = state.checkedAccounts,
                    currentlySkippedAccountIds = currentSkippedAccountIds,
                )
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
                RCore.string.crossAppLoginIntegrityError
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

    private sealed interface AccountCheckResult {
        data class Valid(val account: ExternalAccount) : AccountCheckResult
        sealed interface Issue : AccountCheckResult {
            data object Network : Issue
            data object Other : Issue
        }
    }

    companion object {
        private val TAG = BaseCrossAppLoginViewModel::class.java.simpleName

        fun List<ExternalAccount>.filterSelectedAccounts(skippedIds: Set<Long>): List<ExternalAccount> {
            return if (isNotEmpty()) filter { it.id !in skippedIds } else this
        }
    }
}
