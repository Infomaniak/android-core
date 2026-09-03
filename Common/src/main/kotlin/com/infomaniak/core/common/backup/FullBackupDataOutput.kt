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
import android.app.backup.FullBackupDataOutput
import android.os.Build.VERSION.SDK_INT
import splitties.bitflags.hasFlag

/**
 * If true, we don't have the 25MB limit.
 *
 * Before API 28, we can't know if it's a device-to-device transfer, or a cloud backup.
 * Since it's going through the same pipeline either way, and since the 25MB limit
 * is applied in both cases, we consider that we're not in the device-to-device transfer case.
 */
val FullBackupDataOutput.isDeviceToDeviceTransfer: Boolean
    get() = if (SDK_INT >= 28) transportFlags.hasFlag(BackupAgent.FLAG_DEVICE_TO_DEVICE_TRANSFER) else false

val FullBackupDataOutput.isClientSideEncryptionEnabled: Boolean
    get() = if (SDK_INT >= 28) transportFlags.hasFlag(BackupAgent.FLAG_CLIENT_SIDE_ENCRYPTION_ENABLED) else false
