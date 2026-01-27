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
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import splitties.init.injectAsAppCtx

@RunWith(RobolectricTestRunner::class)
abstract class BaseAccountUtilsTest {

    protected lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.injectAsAppCtx()
    }

    protected fun userOf(id: Int): User {
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
}
