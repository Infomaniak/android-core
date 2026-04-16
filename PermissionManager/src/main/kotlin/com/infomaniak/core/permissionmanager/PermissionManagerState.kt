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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionManagerState(permissionType: PermissionType): PermissionManagerState {
    val permissionString = permissionType.permission

    return if (permissionString == null) {
        remember { UnsupportedApiPermissionManagerState }
    } else {
        val permissionState = rememberPermissionState(permissionString)
        val shouldDisplayRationale = rememberSaveable { mutableStateOf(false) }
        remember { SupportedApiPermissionManagerState(permissionState, shouldDisplayRationale) }
    }
}

@Stable
sealed interface PermissionManagerState {
    val shouldDisplayRationale: Boolean

    fun askPermission()
    fun dismissAndAskPermission()
}
