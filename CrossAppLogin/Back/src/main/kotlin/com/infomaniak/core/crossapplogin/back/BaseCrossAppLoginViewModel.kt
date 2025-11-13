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
import com.infomaniak.core.DynamicLazyMap
import com.infomaniak.core.Xor
import com.infomaniak.core.auth.api.ApiRepositoryCore
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.cancellable
import com.infomaniak.core.completableScope
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel.AccountCheckingStatus.*
import com.infomaniak.core.crossapplogin.back.DerivedTokenGenerator.Issue
import com.infomaniak.core.crossapplogin.back.internal.CustomTokenInterceptor
import com.infomaniak.core.network.LOGIN_ENDPOINT_URL
import com.infomaniak.core.network.models.exceptions.NetworkException
import com.infomaniak.core.network.networking.HttpClient.addCache
import com.infomaniak.core.network.networking.HttpClient.addCommonInterceptors
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.network.utils.bodyAsStringOrNull
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.lib.login.ApiToken
import io.sentry.IScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.OkHttpClient
import com.infomaniak.core.R as RCore
import com.infomaniak.core.appintegrity.R as RAppIntegrity
import com.infomaniak.core.network.R as RCoreNetwork

abstract class BaseCrossAppLoginViewModel(applicationId: String, clientId: String) : ViewModel() {

    private val _availableAccounts = MutableStateFlow(emptyList<ExternalAccount>())
    val availableAccounts: StateFlow<List<ExternalAccount>> = _availableAccounts.asStateFlow()
    val skippedAccountIds = MutableStateFlow(emptySet<Long>())

    val accountsCheckingState: StateFlow<AccountsCheckingState> = _availableAccounts
        .transformToAccountsCheckingState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AccountsCheckingState(status = WaitingForAccount)
        )

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

    private val accountCheckStatuses = DynamicLazyMap<ExternalAccount, _>(
        cacheManager = { _, _ -> awaitCancellation() },
        coroutineScope = viewModelScope,
        createElement = { account ->
            async {
                val checkingTokensDeferredList = account.tokens.map { token -> getTokenCheckAsync(token) }
                getFirstValidTokenOrError(checkingTokensDeferredList)
            }
        },
    )

    private val apiRepository = object : ApiRepositoryCore() {}

    private val baseOkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addCache()
            addCommonInterceptors()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun activateUpdates(hostActivity: ComponentActivity, singleSelection: Boolean = false): Nothing = coroutineScope {
        // Do nothing if the user's device cannot be verified via Play's AppIntegrity, to avoid displaying the CrossAppLogin
        if (!derivedTokenGenerator.checkIfAppIntegrityCouldSucceed()) awaitCancellation()

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

    private fun StateFlow<List<ExternalAccount>>.transformToAccountsCheckingState(): Flow<AccountsCheckingState> {
        return transform { accounts ->
            if (accounts.isEmpty()) return@transform

            emit(AccountsCheckingState(status = Ongoing))

            // Initial error value. Meant to be replaced as soon as another error happened, it means we had network at some point
            // so we don't want to display something like "no network" to the user
            var checkError: Error = Error.Network
            val checkedAccounts = mutableListOf<ExternalAccount>()

            accounts.forEach { account -> checkAccount(account, checkedAccounts)?.let { error -> checkError = error } }

            emit(
                AccountsCheckingState(
                    checkedAccounts = { checkedAccounts },
                    status = if (checkedAccounts.isNotEmpty()) Complete else checkError,
                )
            )
        }
    }

    /**
     * Check if an account can be derivated, and cache the result in [accountCheckStatuses]
     *
     * Emit an [AccountsCheckingState] with status [AccountCheckingStatus.Ongoing] and the current checked account to allow
     * displaying each account as soon as they're checked.
     *
     * @param account The account that will be checked
     * @param checkedAccounts The mutable list of account that are checked and must be displayed to the user
     *
     * @return an [AccountCheckingStatus.Error] if the check fail or null in other cases
     */
    private suspend fun FlowCollector<AccountsCheckingState>.checkAccount(
        account: ExternalAccount,
        checkedAccounts: MutableList<ExternalAccount>,
    ): Error? = accountCheckStatuses.useElement(account) { statusAsync ->
        val status = statusAsync.await()
        return when (status) {
            Ongoing -> {
                if (checkedAccounts.contains(account).not()) {
                    checkedAccounts.add(account)
                    emit(AccountsCheckingState(status = Ongoing, checkedAccounts = { checkedAccounts }))
                }
                null
            }
            Error.Network -> null // We don't want to replace the possible real errors
            is Error -> status
            WaitingForAccount, Complete -> null // Cannot happened, the checkStatuses don't return these values
        }
    }

    private fun CoroutineScope.getTokenCheckAsync(token: String): Deferred<AccountCheckingStatus> {
        // For each token of the account on the other apps, we check if getting the profile works.
        // If it's the case, the token should be able to be derivated later successfully.
        val customTokenHttpClient = baseOkHttpClient.addInterceptor(CustomTokenInterceptor(token)).build()
        return async {
            runCatching {
                if (apiRepository.getUserProfile(customTokenHttpClient).data is User) {
                    Ongoing
                } else {
                    Error.Unknown
                }
            }.cancellable().getOrElse { exception ->
                when (exception) {
                    is NetworkException -> Error.Network
                    else -> Error.Unknown
                }
            }
        }
    }

    /**
     * Use a [completableScope] to check all token concurrently, and stop as soon as one of them returned a valid data
     *
     * @return The [AccountCheckingStatus] of the check: [Ongoing] if it succeeded, [Error] otherwise
     */
    private suspend fun getFirstValidTokenOrError(
        checkingTokensDeferredList: List<Deferred<AccountCheckingStatus>>
    ): AccountCheckingStatus = completableScope { completable ->
        var accountCheckingError: Error = Error.Network // Default error case

        checkingTokensDeferredList.forEach { checkToken ->
            launch {
                when (val result = checkToken.await()) {
                    is Ongoing -> completable.complete(result)
                    // If at least one error is not network, we change the error to avoid displaying network error
                    Error.Unknown -> accountCheckingError = Error.Unknown
                    else -> Unit
                }
            }
        }

        accountCheckingError // Will make it only if there is no match after all child coroutines complete.
    }

    /**
     * Ensures that there's only one selected account, even through updates to [accountsCheckingState] and [skippedAccountIds].
     */
    private suspend fun keepSingleSelection(): Nothing {
        accountsCheckingState.collectLatest { state ->
            skippedAccountIds.collectLatest { currentSkippedAccountIds ->
                skippedAccountIds.value = newSkippedAccountIdsToKeepSingleSelection(
                    accounts = state.checkedAccounts(),
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

    data class AccountsCheckingState(
        val status: AccountCheckingStatus,
        val checkedAccounts: () -> List<ExternalAccount> = { emptyList() },
    )

    sealed interface AccountCheckingStatus {
        data object WaitingForAccount : AccountCheckingStatus
        data object Ongoing : AccountCheckingStatus
        data object Complete : AccountCheckingStatus
        sealed class Error : AccountCheckingStatus {
            data object Network : Error()
            data object Unknown : Error()
        }
    }

    companion object {
        private val TAG = BaseCrossAppLoginViewModel::class.java.simpleName

        fun List<ExternalAccount>.filterSelectedAccounts(skippedIds: Set<Long>): List<ExternalAccount> {
            return if (isNotEmpty()) filter { it.id !in skippedIds } else this
        }
    }
}
