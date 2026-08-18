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
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.Xor
import com.infomaniak.core.login.ApiToken
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
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

    // Android ID set by BaseAccountUtilsTest as "test_android_id"
    private val currentAndroidId = "test_android_id"
    private val otherDeviceAndroidId = "other_device_id"

    // ------------------------------------------------------------------ helpers

    private fun TestScope.createManager(
        userDatabase: UserDatabase,
        tokenGenerator: DerivedTokenGenerator = FakeDerivedTokenGenerator(),
    ) = RestoreFromBackupManagerImpl(
        coroutineScope = backgroundScope,
        userDatabase = userDatabase,
        tokenGenerator = tokenGenerator,
    )

    private fun newToken(accessToken: String = "new_token"): ApiToken =
        ApiToken(accessToken = accessToken, tokenType = "Bearer", userId = 0, expiresIn = 3600)

    private fun networkFailure(): Xor<ApiToken, DerivedTokenGenerator.Issue> =
        Xor.Second(DerivedTokenGenerator.Issue.NetworkIssue(IOException("network error")))

    /** Inserts a user plus a [TokenDeviceBinding] recording which device created the token. */
    private suspend fun UserDatabase.insertUserWithBinding(userId: Int, androidId: String) {
        userDao().insert(userOf(userId))
        userDao().upsertTokenDeviceBinding(TokenDeviceBinding(userId, androidId))
    }

    /** Inserts a user with **no** [TokenDeviceBinding] – simulates a pre-v9 database row. */
    private suspend fun UserDatabase.insertUserWithoutBinding(userId: Int) {
        userDao().insert(userOf(userId))
    }

    /**
     * Collects [State] values emitted by [RestoreFromBackupManager.state] until [State.Settled],
     * returning the full sequence (inclusive). Any [State.RestoringFromBackupFailed] encountered
     * is forwarded to [onFailed] before the next state is awaited.
     */
    private suspend fun RestoreFromBackupManager.collectStatesUntilSettled(
        onFailed: (State.RestoringFromBackupFailed) -> Unit = {},
    ): List<State> {
        val states = mutableListOf<State>()
        state.first { s ->
            states += s
            if (s is State.RestoringFromBackupFailed) onFailed(s)
            s is State.Settled
        }
        return states
    }

    // --------------------------------------------------- no users

    @Test
    fun noUsers_settlesImmediately() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        val manager = createManager(db)
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.Settled)
        db.close()
    }

    // --------------------------------------------------- same device

    @Test
    fun sameDevice_settlesImmediately() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithBinding(userId = 1, androidId = currentAndroidId)
        val manager = createManager(db)
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.Settled)
        db.close()
    }

    @Test
    fun multipleUsers_allSameDevice_settlesImmediately() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithBinding(userId = 1, androidId = currentAndroidId)
        db.insertUserWithBinding(userId = 2, androidId = currentAndroidId)
        db.insertUserWithBinding(userId = 3, androidId = currentAndroidId)
        val manager = createManager(db)
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.Settled)
        db.close()
    }

    // --------------------------------------------------- pre-v9: missing binding

    @Test
    fun missingBinding_addsBindingAndSettles() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithoutBinding(userId = 1)
        val manager = createManager(db)
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled()

        // Missing binding means a same-device app upgrade (pre-v9); no token derivation needed.
        states shouldBe listOf(State.Settled)
        db.userDao().getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        db.close()
    }

    @Test
    fun multipleUsers_allMissingBindings_addsBindingsAndSettles() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithoutBinding(userId = 1)
        db.insertUserWithoutBinding(userId = 2)
        val manager = createManager(db)
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.Settled)
        db.userDao().getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        db.userDao().getTokenDeviceBindingForUser(2)?.androidId shouldBe currentAndroidId
        db.close()
    }

    // --------------------------------------------------- transferred device – success

    @Test
    fun transferredDevice_derivationSucceeds_updatesTokenAndSettles() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)

        val manager = createManager(db, FakeDerivedTokenGenerator(Xor.First(newToken("derived_token"))))
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.RestoringFromBackup, State.Settled)
        db.userDao().findById(1)?.apiToken?.accessToken shouldBe "derived_token"
        db.userDao().getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        db.close()
    }

    @Test
    fun multipleUsers_allTransferred_allSucceed_settles() = runTest(timeout = 2.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)
        db.insertUserWithBinding(userId = 2, androidId = otherDeviceAndroidId)
        val manager = createManager(db, FakeDerivedTokenGenerator(Xor.First(newToken("derived"))))
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.RestoringFromBackup, State.Settled)
        db.userDao().getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        db.userDao().getTokenDeviceBindingForUser(2)?.androidId shouldBe currentAndroidId
        db.close()
    }

    @Test
    fun multipleUsers_mixedDevices_onlyTransferredAreRestored() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithBinding(userId = 1, androidId = currentAndroidId)      // already valid
        db.insertUserWithBinding(userId = 2, androidId = otherDeviceAndroidId)  // needs restoration
        val manager = createManager(db, FakeDerivedTokenGenerator(Xor.First(newToken("derived"))))
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled()

        states shouldBe listOf(State.RestoringFromBackup, State.Settled)
        db.userDao().getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        db.userDao().getTokenDeviceBindingForUser(2)?.androidId shouldBe currentAndroidId
        db.close()
    }

    // --------------------------------------------------- restoration failure

    @Test
    fun transferredDevice_derivationFails_emitsRestoringFromBackupFailedState() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)

        val manager = createManager(db, FakeDerivedTokenGenerator(networkFailure()))
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled { failedState ->
            failedState.giveUp() // unblock the flow so the test completes
        }

        states.any { it is State.RestoringFromBackupFailed }.shouldBeTrue()
        val failed = states.filterIsInstance<State.RestoringFromBackupFailed>().first()
        failed.cause.shouldBeInstanceOf<DerivedTokenGenerator.Issue.NetworkIssue>()
        db.close()
    }

    // --------------------------------------------------- retry

    @Test
    fun transferredDevice_derivationFails_thenRetry_succeeds() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)

        val generator = FakeDerivedTokenGenerator(networkFailure())
        val manager = createManager(db, generator)
        manager.registerRemoveUser { }

        val states = manager.collectStatesUntilSettled { failedState ->
            // Switch to success on retry.
            generator.result = Xor.First(newToken("derived_after_retry"))
            failedState.retry()
        }

        // Attempt 1 → failure → retry → success → Settled
        states.any { it is State.RestoringFromBackup }.shouldBeTrue()
        states.count { it is State.RestoringFromBackupFailed } shouldBe 1
        states.last().shouldBeInstanceOf<State.Settled>()
        // Confirm the retry produced the new token and updated the binding.
        db.userDao().findById(1)?.apiToken?.accessToken shouldBe "derived_after_retry"
        db.userDao().getTokenDeviceBindingForUser(1)?.androidId shouldBe currentAndroidId
        db.close()
    }

    // --------------------------------------------------- give-up

    @Test
    fun transferredDevice_derivationFails_giveUp_removesFailedUserAndSettles() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        db.insertUserWithBinding(userId = 1, androidId = otherDeviceAndroidId)

        val manager = createManager(db, FakeDerivedTokenGenerator(networkFailure()))

        val removedUserIds = mutableListOf<Int>()
        manager.registerRemoveUser { id -> removedUserIds += id }

        manager.collectStatesUntilSettled { failedState -> failedState.giveUp() }

        removedUserIds shouldBe listOf(1)
        db.close()
    }

    @Test
    fun multipleUsers_partialFailure_giveUp_onlyRemovesFailedUser() = runTest(timeout = .1.seconds) {
        val db = UserDatabase.instantiateDataBase(context, true)
        // User 1 is on the current device: no restoration required, must not be removed.
        db.insertUserWithBinding(userId = 1, androidId = currentAndroidId)
        // User 2 was transferred: restoration will fail.
        db.insertUserWithBinding(userId = 2, androidId = otherDeviceAndroidId)

        val manager = createManager(db, FakeDerivedTokenGenerator(networkFailure()))

        val removedUserIds = mutableListOf<Int>()
        manager.registerRemoveUser { id -> removedUserIds += id }

        manager.collectStatesUntilSettled { failedState -> failedState.giveUp() }

        // Only the user whose restoration failed must be removed.
        removedUserIds shouldBe listOf(2)
        db.close()
    }

    // ------------------------------------------------------------------ fake

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
}
