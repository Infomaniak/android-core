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

import com.infomaniak.core.auth.BaseAccountUtilsTest
import com.infomaniak.core.auth.DerivedTokenGenerator
import com.infomaniak.core.auth.backup.RestoreFromBackupManager.State
import com.infomaniak.core.auth.models.TokenDeviceBinding
import com.infomaniak.core.auth.room.UserDao
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.Xor
import com.infomaniak.core.login.ApiToken
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for [RestoreFromBackupManagerImpl], covering all paths in the restoration state machine:
 * - Unchanged device (no restoration needed)
 * - Pre-v9 missing binding (binding added, no restoration)
 * - Transferred device with successful derivation
 * - Multiple accounts (all same device / all transferred / mixed)
 * - Partial failures
 * - Retry after failure
 * - Give-up after failure (failed users removed)
 */
class RestoreFromBackupManagerImplTest : BaseAccountUtilsTest() {

    private val currentAndroidId: String = testAndroidId
    private val otherDeviceAndroidId = "other_device_id"

    // --------------------------------------------------- no users

    @Test
    fun noUsers_settlesImmediately() = test { _ ->
        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.Settled)
    }

    // --------------------------------------------------- same device

    @Test
    fun sameDevice_settlesImmediately() = test { userDao ->
        userDao.insertUserWithBinding(userId = 1, androidId = currentAndroidId)

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.Settled)
    }

    @Test
    fun multipleUsers_allSameDevice_settlesImmediately() = test { userDao ->
        userDao.insertUserWithBinding(userId = 1, androidId = currentAndroidId)
        userDao.insertUserWithBinding(userId = 2, androidId = currentAndroidId)
        userDao.insertUserWithBinding(userId = 3, androidId = currentAndroidId)

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.Settled)
    }

    // --------------------------------------------------- pre-v9: missing binding

    @Test
    fun missingBinding_addsBindingAndSettles() = test { userDao ->
        userDao.insertUserWithoutBinding(userId = 1)

        val states = manager.collectStatesUntilSettled()

        // Missing binding means a same-device app upgrade (pre-v9); no token derivation needed.
        states shouldBe listOf(State.Settled)
        userDao.getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
    }

    @Test
    fun multipleUsers_allMissingBindings_addsBindingsAndSettles() = test { userDao ->
        userDao.insertUserWithoutBinding(userId = 1)
        userDao.insertUserWithoutBinding(userId = 2)

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.Settled)
        userDao.getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        userDao.getTokenDeviceBindingForUser(2)?.androidId shouldBe currentAndroidId
    }

    // --------------------------------------------------- transferred device – success

    @Test
    fun transferredDevice_derivationSucceeds_updatesTokenAndSettles() = test { userDao ->
        userDao.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.RestoringFromBackup, State.Settled)
        userDao.findById(1)?.apiToken?.accessToken shouldBe "derived_token"
        userDao.getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
    }

    @Test
    fun multipleUsers_allTransferred_allSucceed_settles() = test(timeout = 2.seconds) { userDao ->
        userDao.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)
        userDao.insertUserWithBinding(userId = 2, androidId = otherDeviceAndroidId)

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.RestoringFromBackup, State.Settled)
        userDao.getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        userDao.getTokenDeviceBindingForUser(2)?.androidId shouldBe currentAndroidId
    }

    @Test
    fun multipleUsers_mixedDevices_onlyTransferredAreRestored() = test { userDao ->
        userDao.insertUserWithBinding(userId = 1, androidId = currentAndroidId)      // already valid
        userDao.insertUserWithBinding(userId = 2, androidId = otherDeviceAndroidId)  // needs restoration

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.RestoringFromBackup, State.Settled)
        userDao.getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        userDao.getTokenDeviceBindingForUser(2)?.androidId shouldBe currentAndroidId
    }

    // --------------------------------------------------- restoration failure

    @Test
    fun transferredDevice_derivationFails_emitsRestoringFromBackupFailedState() = test(
        initialTokenDerivationResult = networkFailure()
    ) { userDao ->
        userDao.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)

        val states = manager.collectStatesUntilSettled { failedState ->
            failedState.giveUp() // unblock the flow so the test completes
        }

        states.any { it is State.RestoringFromBackupFailed }.shouldBeTrue()
        val failed = states.filterIsInstance<State.RestoringFromBackupFailed>().first()
        failed.cause.shouldBeInstanceOf<DerivedTokenGenerator.Issue.NetworkIssue>()
    }

    // --------------------------------------------------- retry

    @Test
    fun transferredDevice_derivationFails_thenRetry_succeeds() = test(
        initialTokenDerivationResult = networkFailure()
    ) { userDao ->
        userDao.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)

        val states = manager.collectStatesUntilSettled { failedState ->
            // Switch to success on retry.
            derivedTokenGenerator.result = Xor.First(newToken("derived_after_retry"))
            failedState.retry()
        }

        // Attempt 1 → failure → retry → success → Settled
        states.any { it is State.RestoringFromBackup }.shouldBeTrue()
        states.count { it is State.RestoringFromBackupFailed } shouldBe 1
        states.last().shouldBeInstanceOf<State.Settled>()
        // Confirm the retry produced the new token and updated the binding.
        userDao.findById(1)?.apiToken?.accessToken shouldBe "derived_after_retry"
        userDao.getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
    }

    // --------------------------------------------------- give-up

    @Test
    fun transferredDevice_derivationFails_giveUp_removesFailedUserAndSettles() = test(
        initialTokenDerivationResult = networkFailure()
    ) { userDao ->
        userDao.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)

        manager.collectStatesUntilSettled { failedState ->
            userDao.findById(1).shouldNotBeNull()
            failedState.giveUp()
        }

        userDao.findById(1).shouldBeNull()
    }

    @Test
    fun multipleUsers_partialFailure_giveUp_onlyRemovesFailedUser() = test(
        initialTokenDerivationResult = networkFailure()
    ) { userDao ->
        // User 1 is on the current device: no restoration required, must not be removed.
        userDao.insertUserWithBinding(userId = 1, androidId = currentAndroidId)
        // User 2 was transferred: restoration will fail.
        userDao.insertUserWithBinding(userId = 2, androidId = otherDeviceAndroidId)

        manager.collectStatesUntilSettled { failedState -> failedState.giveUp() }

        // Only the user whose restoration failed must be removed.
        userDao.findById(2) shouldBe null
    }

    // ------------------------------------------------------------------ helpers

    private fun test(
        timeout: Duration = .1.seconds,
        initialTokenDerivationResult: Xor<ApiToken, DerivedTokenGenerator.Issue> = Xor.First(newToken("derived_token")),
        block: suspend RestoreFromBackupTestScope.(userDao: UserDao) -> Unit
    ) {
        runTest(timeout = timeout) {
            val userDatabase = UserDatabase.instantiateDataBase(context, inMemory = true)
            val restoreFromBackupTestScope = RestoreFromBackupTestScope(
                testScope = this,
                userDatabase = userDatabase,
                initialTokenDerivationResult = initialTokenDerivationResult,
            )
            try {
                restoreFromBackupTestScope.block(userDatabase.userDao())
            } finally {
                userDatabase.close()
            }
        }
    }

    /** Inserts a user plus a [TokenDeviceBinding] recording which device created the token. */
    private suspend fun UserDao.insertUserWithBinding(userId: Int, androidId: String) {
        insert(userOf(userId))
        upsertTokenDeviceBinding(TokenDeviceBinding(userId, androidId))
    }

    /** Inserts a user with **no** [TokenDeviceBinding] – simulates a pre-v9 database row. */
    private suspend fun UserDao.insertUserWithoutBinding(userId: Int) {
        insert(userOf(userId))
    }

    /**
     * Collects [State] values emitted by [state] until [State.Settled],
     * returning the full sequence (inclusive). Any [State.RestoringFromBackupFailed] encountered
     * is forwarded to [onFailed] before the next state is awaited.
     */
    private suspend fun RestoreFromBackupManager.collectStatesUntilSettled(
        onFailed: suspend (State.RestoringFromBackupFailed) -> Unit = {},
    ): List<State> {
        val states = mutableListOf<State>()
        state.first { s ->
            states += s
            if (s is State.RestoringFromBackupFailed) onFailed(s)
            s is State.Settled
        }
        return states
    }

    private class FakeDerivedTokenGenerator(
        var result: Xor<ApiToken, DerivedTokenGenerator.Issue> = Xor.First(
            ApiToken(accessToken = "new_token", tokenType = "Bearer", userId = 0, expiresIn = 3600)
        ),
    ) : DerivedTokenGenerator {
        override suspend fun attemptDerivingOneOfTheseTokens(
            tokensToTry: Set<String>,
        ): Xor<ApiToken, DerivedTokenGenerator.Issue> = result

        override suspend fun isAppIntegrityGuaranteedToFail() = false
    }

    private class RestoreFromBackupTestScope(
        testScope: TestScope,
        userDatabase: UserDatabase,
        initialTokenDerivationResult: Xor<ApiToken, DerivedTokenGenerator.Issue>,
    ) {
        val derivedTokenGenerator = FakeDerivedTokenGenerator(initialTokenDerivationResult)
        val manager: RestoreFromBackupManager by lazy {
            RestoreFromBackupManagerImpl(
                coroutineScope = testScope.backgroundScope,
                userDatabase = userDatabase,
                tokenGenerator = derivedTokenGenerator
            ).also {
                it.registerRemoveUser { id -> userDatabase.userDao().deleteUserById(id) }
            }
        }
    }
}

private fun newToken(accessToken: String = "new_token"): ApiToken =
    ApiToken(accessToken = accessToken, tokenType = "Bearer", userId = 0, expiresIn = 3600)

private fun networkFailure(): Xor<ApiToken, DerivedTokenGenerator.Issue> =
    Xor.Second(DerivedTokenGenerator.Issue.NetworkIssue(IOException("network error")))
