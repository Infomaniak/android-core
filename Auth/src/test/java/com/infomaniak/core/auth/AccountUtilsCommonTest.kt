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

import android.database.sqlite.SQLiteConstraintException
import com.infomaniak.core.auth.room.UserDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class AccountUtilsCommonTest : BaseAccountUtilsTest() {

    @Test
    fun addUser_addsUsersCorrectly() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            addUser(userOf(id = 3))

            Assert.assertEquals(listOf(1, 2, 3), users.first().map { it.id })
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun addUser_throwsWhenAddingTheSameUserTwice() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 1))
        }
    }

    @Test
    fun addUser_removesUsersCorrectly() = runTest {
        withAccountUtils {
            addUser(userOf(id = 1))
            addUser(userOf(id = 2))
            addUser(userOf(id = 3))

            removeUser(3)
            removeUser(1)
            Assert.assertEquals(listOf(2), users.first().map { it.id })
        }
    }

    @Test
    fun addUser_removingNonExistentUserDoesNothing() = runTest {
        withAccountUtils {
            removeUser(1)
            Assert.assertEquals(emptyList<Int>(), users.first().map { it.id })
        }
    }

    private inline fun withAccountUtils(block: UserAccountUtils.() -> Unit) {
        val userDatabase = UserDatabase.instantiateDataBase(context, true)
        val persistedUserIdAccountUtils = object : UserAccountUtils(context, userDatabase = userDatabase) {}
        block(persistedUserIdAccountUtils)
        userDatabase.close()
    }
}
