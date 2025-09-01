/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.core.ksuite.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface KSuite : Parcelable {

    sealed interface Perso : KSuite {
        @Parcelize
        data object Free : Perso
        @Parcelize
        data object Plus : Perso
    }

    sealed interface Pro : KSuite {
        @Parcelize
        data object Free : Pro
        @Parcelize
        data object Standard : Pro
        @Parcelize // Unused in kMail, all Pro paid tiers got the same functionalities in kMail, so [Pro.Standard] is enough
        data object Business : Pro
        @Parcelize // Unused in kMail, all Pro paid tiers got the same functionalities in kMail, so [Pro.Standard] is enough
        data object Enterprise : Pro
    }

    fun isProUpgradable(): Boolean = this is Pro && this != Pro.Enterprise
}
