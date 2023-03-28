/*
 * Infomaniak Core - Android
 * Copyright (C) 2023 Infomaniak Network SA
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
package com.infomaniak.lib.core.utils

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.infomaniak.lib.core.fdroidTools.FdroidApiTools
import kotlinx.coroutines.launch

fun FragmentActivity.checkUpdateIsAvailable(appId: String, versionCode: Int, onResult: (updateIsAvailable: Boolean) -> Unit) {
    lifecycleScope.launch {
        val lastVersionCode = FdroidApiTools().getLastRelease(appId)

        onResult(versionCode < lastVersionCode)
    }
}

fun FragmentActivity.launchInAppReview() = Unit