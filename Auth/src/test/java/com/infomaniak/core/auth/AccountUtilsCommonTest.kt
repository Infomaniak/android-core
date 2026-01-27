/*
 * Infomaniak SwissTransfer - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * Unit tests for PersistedUserIdAccountUtils public methods.
 */
package com.infomaniak.core.auth

import android.database.sqlite.SQLiteConstraintException
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

            Assert.assertEquals(listOf(1, 2, 3), allUsers.first().map { it.id })
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
            Assert.assertEquals(listOf(2), allUsers.first().map { it.id })
        }
    }

    @Test
    fun addUser_removingNonExistentUserDoesNothing() = runTest {
        withAccountUtils {
            removeUser(1)
            Assert.assertEquals(emptyList<Int>(), allUsers.first().map { it.id })
        }
    }

    private inline fun withAccountUtils(block: AccountUtilsCommon.() -> Unit) {
        val persistedUserIdAccountUtils = object : AccountUtilsCommon(context, inMemory = true) {}
        block(persistedUserIdAccountUtils)
        persistedUserIdAccountUtils.userDatabase.close()
    }
}
