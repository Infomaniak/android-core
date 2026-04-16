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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first

@Stable
@OptIn(ExperimentalPermissionsApi::class)
internal class SupportedApiPermissionManagerState(
    private val permissionState: PermissionState,
    private val _shouldShowRationale: MutableState<Boolean>,
) : PermissionManagerState {
    override val shouldShowRationale: Boolean by _shouldShowRationale

    override fun askPermission() {
        if (permissionState.status.isGranted) return

        if (permissionState.status.shouldShowRationale) {
            _shouldShowRationale.value = true
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    override fun dismissAndAskPermission() {
        _shouldShowRationale.value = false
        permissionState.launchPermissionRequest()
    }

    @Composable
    override fun waitUntilGranted(action: () -> Unit): () -> Unit = with(permissionState) {
        val isGrantedFlow = remember(permission) { snapshotFlow { status.isGranted } }
        val actionFired: CompletableJob = remember(permission) { Job() }

        LaunchedEffect(permission) {
            actionFired.join()
            isGrantedFlow.first { it }
            action()
        }

        return fun() {
            if (status.isGranted) {
                action()
            } else {
                launchPermissionRequest()
                actionFired.complete()
            }
        }
    }
}
