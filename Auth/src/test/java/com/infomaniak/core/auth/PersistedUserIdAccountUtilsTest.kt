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

class PersistedUserIdAccountUtilsTest : BaseAccountUtilsTest() {

    @Test
    fun addUser_selectsTheLastAddedUser() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))

            Assert.assertEquals(2, currentUserIdFlow.first())
            Assert.assertEquals(2, currentUserFlow.first()?.id)
        }
    }

    @Test
    fun removeUser_unselectsTheCurrentAccount() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            Assert.assertEquals(1, currentUserIdFlow.first())
            Assert.assertEquals(1, currentUserFlow.first()?.id)

            removeUser(1)
            Assert.assertNull(currentUserIdFlow.first())
            Assert.assertNull(currentUserFlow.first())
        }
    }

    @Test
    fun removeUserAndSwitchToNext_selectsTheNextUserCorrectly() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            removeUserAndSwitchToNext(2)
            Assert.assertEquals(1, currentUserIdFlow.first())
            Assert.assertEquals(1, currentUserFlow.first()?.id)
        }
    }

    @Test
    fun removeUserAndSwitchToNext_unselectsCompletelyWhenRemovingLastUser() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            removeUserAndSwitchToNext(1)
            Assert.assertNull(currentUserIdFlow.first())
            Assert.assertNull(currentUserFlow.first())
        }
    }

    @Test
    fun switchUser_correctlySwitchToExistingAccounts() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            switchUser(1)
            Assert.assertEquals(1, currentUserIdFlow.first())
            Assert.assertEquals(1, currentUserFlow.first()?.id)
        }
    }

    @Test
    fun removeUserAndSwitchToNext_switchesCorrectly() = runTest {
        withAccountUtils {
            // Add in specific order: 1, 2, 3
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            addUser(userOf(id = 3))

            // Current is 3. Remove 3, should select 1
            removeUserAndSwitchToNext(3)
            Assert.assertEquals(1, currentUserIdFlow.first())
            Assert.assertEquals(1, currentUserFlow.first()?.id)

            // Remove 1, should select 2
            removeUserAndSwitchToNext(1)
            Assert.assertEquals(2, currentUserIdFlow.first())
            Assert.assertEquals(2, currentUserFlow.first()?.id)

            // Remove 2, should unselect
            removeUserAndSwitchToNext(2)
            Assert.assertNull(currentUserIdFlow.first())
            Assert.assertNull(currentUserFlow.first())
        }
    }

    @Test
    fun switchUser_doesNothingWhenSwitchingToAccountsThatDontExist() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            switchUser(999)
            Assert.assertEquals(2, currentUserIdFlow.first())
            Assert.assertEquals(2, currentUserFlow.first()?.id)
        }
    }

    @Test
    fun switchUser_doesNothingWhenSwitchingToCurrentAccount() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            switchUser(2)
            Assert.assertEquals(2, currentUserIdFlow.first())
            Assert.assertEquals(2, currentUserFlow.first()?.id)
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

    @Test
    fun currentUserIdFlow_isNullInitially() = runTest {
        withAccountUtils {
            Assert.assertNull(currentUserIdFlow.first())
        }
    }

    @Test
    fun currentUserFlow_canBeReadMultipleTimesInARow() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))

            Assert.assertEquals(1, currentUserFlow.first()?.id)
            Assert.assertEquals(1, currentUserFlow.first()?.id)
        }
    }

    @Test
    fun currentUserFlow_isNullInitially() = runTest {
        withAccountUtils {
            Assert.assertNull(currentUserFlow.first())
        }
    }

    private inline fun withAccountUtils(block: PersistedUserIdAccountUtils.() -> Unit) {
        val persistedUserIdAccountUtils = object : PersistedUserIdAccountUtils(context, inMemory = true) {}
        block(persistedUserIdAccountUtils)
        persistedUserIdAccountUtils.userDatabase.close()
    }
}
