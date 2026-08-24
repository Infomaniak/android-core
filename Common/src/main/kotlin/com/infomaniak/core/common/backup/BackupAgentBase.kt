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

import android.app.backup.BackupAgentHelper
import android.app.backup.BackupDataInputStream
import android.app.backup.BackupDataOutput
import android.app.backup.BackupHelper
import android.os.ParcelFileDescriptor

abstract class BackupAgentBase : BackupAgentHelper() {

    /**
     * Consider using the [BackupHandler] class for [BackupHelper] implementations,
     * to have the correct parameters nullability.
     *
     * @return The key prefixes associated with their helper.
     * @see addHelper
     * @see BackupHandler
     */
    abstract fun createBackupHelpers(): Map<String, BackupHelper>

    /**
     * Defines the nullability for easier implementation.
     *
     * @see BackupHelper
     */
    abstract class BackupHandler : BackupHelper {
        abstract override fun performBackup(
            oldState: ParcelFileDescriptor?,
            data: BackupDataOutput,
            newState: ParcelFileDescriptor
        )

        abstract override fun restoreEntity(data: BackupDataInputStream)

        abstract override fun writeNewStateDescription(newState: ParcelFileDescriptor)
    }

    final override fun onCreate() {
        val handlers = createBackupHelpers()
        handlers.forEach { (keyPrefix, handler) ->
            addHelper(keyPrefix, handler)
        }
    }
}
