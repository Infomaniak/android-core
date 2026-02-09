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

import com.infomaniak.core.auth.room.UserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    fun removeUser_setsCurrentUserToNullWhenThereAreNoMoreAccounts() = runTest {
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
    fun removeUser_selectsTheNextUserWhenThereAreOtherAccounts() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            removeUser(2)
            Assert.assertEquals(1, currentUserIdFlow.first())
            Assert.assertEquals(1, currentUserFlow.first()?.id)
        }
    }

    @Test
    fun removeUser_doesntSwitchUserWhenRemovedUserIsNotTheCurrent() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            addUser(userOf(id = 3))

            switchUser(2)

            removeUser(3)
            Assert.assertEquals(2, currentUserIdFlow.first())
            Assert.assertEquals(2, currentUserFlow.first()?.id)
        }
    }

    /**
     * We want to collect id: 2 -> id: 1 and not id: 2 -> null -> id: 1
     */
    @Test
    fun removeUser_doesntEmitIntermediateNullUser() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))

            val job = assertFlowCollection(listOf(1), currentUserFlow, transform = { it?.id })

            removeUser(2)

            job.join()
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

    /**
     * Ensures we don't change the order without noticing
     */
    @Test
    fun removeUser_switchesInTheCorrectOrder() = runTest {
        withAccountUtils {
            // Add in specific order: 1, 2, 3
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            addUser(userOf(id = 3))

            // Current is 3. Remove 3, should select 1
            removeUser(3)
            Assert.assertEquals(1, currentUserIdFlow.first())
            Assert.assertEquals(1, currentUserFlow.first()?.id)

            // Remove 1, should select 2
            removeUser(1)
            Assert.assertEquals(2, currentUserIdFlow.first())
            Assert.assertEquals(2, currentUserFlow.first()?.id)

            // Remove 2, should unselect
            removeUser(2)
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
    fun switchUser_collectOnlyOnceWhenSwitchingToCurrentAccount() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))

            val job = assertFlowCollection(listOf(2, 1), currentUserIdFlow)

            switchUser(2)
            switchUser(2)
            switchUser(1)

            job.join()
        }
    }

    /**
     * Verifies that the last persisted state can be collected repeatedly, even after it has already been emitted once.
     */
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

    /**
     * Verifies that the last persisted state can be collected repeatedly, even after it has already been emitted once.
     */
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

    private inline fun withAccountUtils(block: PersistedCurrentUserAccountUtils.() -> Unit) {
        val userDatabase = UserDatabase.instantiateDataBase(context, true)
        val persistedUserIdAccountUtils = object : PersistedCurrentUserAccountUtils(context, userDatabase = userDatabase) {}
        val result = runCatching {
            block(persistedUserIdAccountUtils)
        }
        userDatabase.close()
        result.getOrThrow()
    }
}

private fun <T> CoroutineScope.assertFlowCollection(
    expectedValues: List<Any>,
    flow: Flow<T?>,
    transform: (T?) -> Any? = { it },
): Job {
    val mutableExpectedValues = expectedValues.toMutableList()

    return launch {
        flow.collect {
            val cursor = mutableExpectedValues.removeAt(0)
            Assert.assertEquals(cursor, transform(it))

            if (mutableExpectedValues.isEmpty()) cancel()
        }
    }
}
