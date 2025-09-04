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
package com.infomaniak.core.twofactorauth.back

import androidx.lifecycle.ViewModel

class TwoFactorAuthViewModel : ViewModel() {

    //TODO: Every time the app is brought to foreground (leaving to door open to implement limits later on),
    // we want to check if any connected user has a pending challenge to confirm login.
    //
    //TODO: For each of these challenges, we want to keep the state of whether it was locally dismissed or not,
    // to not show it a second time as the user navigates through screen where the confirm login bottom sheet,
    // can pop up.
}
