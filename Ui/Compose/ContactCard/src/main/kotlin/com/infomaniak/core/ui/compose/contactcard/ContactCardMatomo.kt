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
package com.infomaniak.core.ui.compose.contactcard

import com.infomaniak.core.matomo.Matomo

object ContactCardMatomo {
    private const val CONTACT_CARD_CATEGORY = "contactCard"

    private var matomoInstance: Matomo? = null

    fun init(matomo: Matomo) {
        matomoInstance = matomo
    }

    fun trackEvent(name: String) {
        matomoInstance?.trackEvent(CONTACT_CARD_CATEGORY, name)
    }

    fun trackScreen(screenName: String) {
        matomoInstance?.trackScreen(path = screenName, title = screenName)
    }
}
