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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.models.user.preferences.Country
import com.infomaniak.core.auth.models.user.preferences.Language
import com.infomaniak.core.auth.models.user.preferences.OrganizationPreference
import com.infomaniak.core.auth.models.user.preferences.Preferences
import com.infomaniak.lib.login.ApiToken
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import splitties.init.injectAsAppCtx

@RunWith(RobolectricTestRunner::class)
abstract class BaseAccountUtilsTest {

    protected var context: Context = ApplicationProvider.getApplicationContext()

    init {
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
