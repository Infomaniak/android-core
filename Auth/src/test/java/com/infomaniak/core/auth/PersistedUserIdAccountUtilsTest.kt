/*
 * Infomaniak SwissTransfer - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * Unit tests for PersistedUserIdAccountUtils public methods.
 */
package com.infomaniak.core.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class PersistedUserIdAccountUtilsTest : BaseAccountUtilsTest() {

    @Test
    fun addUser_selectsTheLastAddedUser() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))

            Assert.assertEquals(2, currentUserIdFlow.first())
        }
    }

    @Test
    fun removeUser_unselectsTheCurrentAccount() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            Assert.assertEquals(1, currentUserIdFlow.first())

            removeUser(1)
            Assert.assertEquals(null, currentUserIdFlow.first())
        }
    }

    @Test
    fun removeUserAndSwitchToNext_selectsTheNextUserCorrectly() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            removeUserAndSwitchToNext(2)
            Assert.assertEquals(1, currentUserIdFlow.first())
        }
    }

    @Test
    fun removeUserAndSwitchToNext_unselectsCompletelyWhenRemovingLastUser() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            removeUserAndSwitchToNext(1)
            Assert.assertEquals(null, currentUserIdFlow.first())
        }
    }

    @Test
    fun switchUser_correctlySelectsExistingAccounts() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            switchUser(1)
            Assert.assertEquals(1, currentUserIdFlow.first())
        }
    }

    @Test
    fun switchUser_doesNothingWhenSelectingAccountsThatDontExist() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            switchUser(3)
            Assert.assertEquals(2, currentUserIdFlow.first())
        }
    }

    @Test
    fun currentUserIdFlow_canBeReadMultipleTimesInARow() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))

            Assert.assertEquals(1, currentUserIdFlow.first())
            Assert.assertEquals(1, currentUserIdFlow.first())
        }
    }

    // TODO: Test currentUser

    private inline fun withAccountUtils(block: PersistedUserIdAccountUtils.() -> Unit) {
        val persistedUserIdAccountUtils = object : PersistedUserIdAccountUtils(context, inMemory = true) {}
        block(persistedUserIdAccountUtils)
        persistedUserIdAccountUtils.userDatabase.close()
    }
}
