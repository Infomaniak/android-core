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
package com.infomaniak.core.permissionmanager

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale

@Stable
@OptIn(ExperimentalPermissionsApi::class)
internal class SupportedApiPermissionManagerState(
    private val permissionState: PermissionState,
    private val _shouldDisplayRationale: MutableState<Boolean>,
) : PermissionManagerState {
    override val shouldDisplayRationale: Boolean by _shouldDisplayRationale

    override fun askPermission() {
        if (permissionState.status.shouldShowRationale) {
            _shouldDisplayRationale.value = true
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    override fun dismissAndAskPermission() {
        _shouldDisplayRationale.value = false
        permissionState.launchPermissionRequest()
    }
}
