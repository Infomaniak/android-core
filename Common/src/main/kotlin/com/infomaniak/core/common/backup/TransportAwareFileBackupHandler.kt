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
package com.infomaniak.core.common.backup

import android.app.backup.BackupDataInputStream
import android.app.backup.BackupDataOutput
import android.app.backup.FileBackupHelper
import android.content.Context
import android.os.ParcelFileDescriptor

class TransportAwareFileBackupHandler(
    context: Context,
    filesToBackup: Map<String, CloudBackupPolicy>,
) : BackupAgentBase.BackupHandler() {

    enum class CloudBackupPolicy {
        Skip, OnlyIfDeviceEncrypted, Always;
    }

    private val deviceToDeviceBackupHelper by lazy {
        FileBackupHelper(context, *filesToBackup.keys.toTypedArray())
    }

    private val deviceEncryptedCloudBackupHelper by lazy {
        val filesToBackup = filesToBackup.mapNotNull { (name, policy) ->
            when (policy) {
                CloudBackupPolicy.OnlyIfDeviceEncrypted, CloudBackupPolicy.Always -> name
                CloudBackupPolicy.Skip -> null
            }
        }.toTypedArray()
        FileBackupHelper(context, *filesToBackup)
    }

    private val fallbackBackupHelper by lazy {
        val filesToBackup = filesToBackup.mapNotNull { (name, policy) ->
            when (policy) {
                CloudBackupPolicy.Always -> name
                CloudBackupPolicy.OnlyIfDeviceEncrypted, CloudBackupPolicy.Skip -> null
            }
        }.toTypedArray()
        FileBackupHelper(context, *filesToBackup)
    }

    override fun performBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput,
        newState: ParcelFileDescriptor
    ) = when {
        data.isDeviceToDeviceTransfer == true -> deviceToDeviceBackupHelper.performBackup(oldState, data, newState)
        data.isClientSideEncryptionEnabled == true -> deviceEncryptedCloudBackupHelper.performBackup(oldState, data, newState)
        else -> fallbackBackupHelper.performBackup(oldState, data, newState)
    }

    override fun restoreEntity(data: BackupDataInputStream) {
        // Forward it to the helper that can handle all the files.
        deviceToDeviceBackupHelper.restoreEntity(data)
    }

    override fun writeNewStateDescription(newState: ParcelFileDescriptor) {
        // Forward it to the helper that can handle all the files.
        deviceToDeviceBackupHelper.writeNewStateDescription(newState)
    }
}
