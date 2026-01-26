/*
 * Infomaniak SwissTransfer - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * Unit tests for PersistedUserIdAccountUtils public methods.
 */
package com.infomaniak.core.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.models.user.preferences.Country
import com.infomaniak.core.auth.models.user.preferences.Language
import com.infomaniak.core.auth.models.user.preferences.OrganizationPreference
import com.infomaniak.core.auth.models.user.preferences.Preferences
import com.infomaniak.lib.login.ApiToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import splitties.init.injectAsAppCtx

@RunWith(RobolectricTestRunner::class)
class PersistedUserIdAccountUtilsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.injectAsAppCtx()
    }

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

    private inline fun withAccountUtils(block: PersistedUserIdAccountUtils.() -> Unit) {
        val persistedUserIdAccountUtils = object : PersistedUserIdAccountUtils(context, inMemory = true) {}
        block(persistedUserIdAccountUtils)
        persistedUserIdAccountUtils.userDatabase.close()
    }
}

private fun userOf(id: Int): User {
    val dummyOrganization = OrganizationPreference(1234, 0L)
    val dummyLanguage = Language("", "", "")
    val dummyCountry = Country("", false)
    val dummyPreferences = Preferences(null, dummyOrganization, dummyLanguage, dummyCountry, null)
    val dummyApiToken = ApiToken("", null, "", 0, id, null, null)

    return User(
        id,
        displayName = "",
        firstname = "",
        lastname = "",
        email = "",
        avatar = null,
        login = "",
        isStaff = false,
        preferences = dummyPreferences,
        phones = null,
        emails = null,
        apiToken = dummyApiToken,
        organizations = ArrayList(),
    )
}
