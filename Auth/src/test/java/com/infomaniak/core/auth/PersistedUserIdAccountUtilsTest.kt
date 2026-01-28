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

    private inline fun withAccountUtils(block: PersistedUserIdAccountUtils.() -> Unit) {
        val userDatabase = UserDatabase.instantiateDataBase(context, true)
        val persistedUserIdAccountUtils = object : PersistedUserIdAccountUtils(context, userDatabase = userDatabase) {}
        block(persistedUserIdAccountUtils)
        userDatabase.close()
    }
}
