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

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInputStream
import android.app.backup.BackupDataOutput
import android.app.backup.FileBackupHelper
import android.os.ParcelFileDescriptor

class TransportAwareFileBackupHandler(
    private val context: BackupAgent,
    private val filesToBackup: Map<String, BackupPolicy>,
) : BackupAgentBase.BackupHandler() {

    enum class BackupPolicy {
        EncryptedDeviceToDeviceOnly,
        DeviceToDeviceOnly,
        DeviceEncryptedCloudAllowed,
        DeviceEncryptedOnly,
        CloudAllowed;
    }

    private val encryptedDeviceToDeviceBackupHelper by lazy {
        FileBackupHelper(context, *filesToBackup.keys.toTypedArray())
    }

    override fun performBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput,
        newState: ParcelFileDescriptor
    ) {
        val helper = fileBackupHelper(data)
        helper.performBackup(oldState, data, newState)
    }

    override fun restoreEntity(data: BackupDataInputStream) {
        // Forward it to the helper that can handle all the files.
        encryptedDeviceToDeviceBackupHelper.restoreEntity(data)
    }

    override fun writeNewStateDescription(newState: ParcelFileDescriptor) {
        // Forward it to the helper that can handle all the files.
        encryptedDeviceToDeviceBackupHelper.writeNewStateDescription(newState)
    }

    private fun allowedPolicies(data: BackupDataOutput): Set<BackupPolicy> = buildSet {
        add(BackupPolicy.CloudAllowed)
        val clientEncrypted = data.isClientSideEncryptionEnabled == true
        if (data.isDeviceToDeviceTransfer == true) {
            add(BackupPolicy.DeviceToDeviceOnly)
            if (clientEncrypted) add(BackupPolicy.EncryptedDeviceToDeviceOnly)
        }
        if (clientEncrypted) {
            add(BackupPolicy.DeviceEncryptedOnly)
            add(BackupPolicy.DeviceEncryptedCloudAllowed)
        }
    }

    private fun fileBackupHelper(data: BackupDataOutput): FileBackupHelper {
        if (data.isDeviceToDeviceTransfer == true && data.isClientSideEncryptionEnabled == true) {
            return encryptedDeviceToDeviceBackupHelper
        }
        val allowedPolicies = allowedPolicies(data)
        val files = filesToBackup.mapNotNull { (name, policy) -> name.takeIf { policy in allowedPolicies } }
        return FileBackupHelper(context, *files.toTypedArray())
    }
}
