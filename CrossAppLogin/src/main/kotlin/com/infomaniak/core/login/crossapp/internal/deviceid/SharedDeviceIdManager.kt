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
package com.infomaniak.core.login.crossapp.internal.deviceid

import android.content.Context
import com.infomaniak.core.login.crossapp.CrossAppDeviceIdRequest
import com.infomaniak.core.login.crossapp.IpcIssuesManager
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
sealed class SharedDeviceIdManager {

    companion object {
        internal val sharedDeviceIdMutex = Mutex()
        internal val storage: SharedDeviceIdStorage = SharedDeviceIdStorage

        internal operator fun invoke(
            context: Context,
            coroutineScope: CoroutineScope,
            ipcIssuesManager: IpcIssuesManager,
            certificateChecker: AppCertificateChecker,
            targetPackageNames: Set<String>
        ): SharedDeviceIdManager = SharedDeviceIdManagerImpl(
            context,
            coroutineScope,
            ipcIssuesManager,
            certificateChecker,
            targetPackageNames
        )
    }

    /**
     * Gives an id for this device, that is shared across all our apps.
     *
     * This might involve generating said id.
     *
     * If the id is ever updated, it will be emitted in this shared flow.
     */
    abstract val crossAppDeviceIdFlow: SharedFlow<Uuid>

    internal abstract suspend fun findCrossAppDeviceId(request: CrossAppDeviceIdRequest): Uuid?
}
