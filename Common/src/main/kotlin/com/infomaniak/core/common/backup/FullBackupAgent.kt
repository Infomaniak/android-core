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

import android.R.attr.data
import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

/**
 * The Android Backup API design and documentation are very confusing, because there are 2 different systems,
 * not clearly stated in the relevant methods, so here's a summary of each system:
 *
 * ## 1. Key/value backup
 *
 * This is a legacy system that gives a lot of flexibility, incremental backups… but only 5MB of storage,
 * and a very confusing API, mainly because of its documentation that lacks clarity about what each function is and should do.
 *
 * ## 2. Full backup / AutoBackup
 *
 * This is also very poorly documented, and the relation between the programmatic APIs and the 2 different XML syntaxes
 * is documented in a very unclear way.
 *
 * Anyway, this API is the one to use, with XML rules, and with overrides of [onFullBackup] and [onRestoreFile] for specific
 * cases, like extracting some data from a DB that shouldn't be fully backed-up, as a temporary file that is staged for backup.
 */
abstract class FullBackupAgent : BackupAgent() {

    /**
     * Helper function to read the right amount of bytes from [data] directly into a ByteArray.
     * Designed to be used in [onRestoreFile] overrides.
     */
    protected fun ParcelFileDescriptor.toByteArray(size: Long): ByteArray {
        val sizeInBytes = size.toInt()
        check(sizeInBytes.toLong() == size)
        return ByteArray(sizeInBytes).also { destination ->
            val readyBytesCount = DataInputStream(FileInputStream(fileDescriptor)).read(destination)
            check(readyBytesCount == destination.size)
        }
    }

    @Suppress("RedundantOverride") // Allows specifying the nullability.
    override fun onFullBackup(data: FullBackupDataOutput) {
        super.onFullBackup(data)
    }

    @Suppress("RedundantOverride") // Allows specifying the nullability.
    override fun onRestoreFile(
        data: ParcelFileDescriptor,
        size: Long,
        destination: File,
        type: Int,
        mode: Long,
        mtime: Long
    ) {
        super.onRestoreFile(data, size, destination, type, mode, mtime)
    }

    // Never called for full backup.
    final override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput?,
        newState: ParcelFileDescriptor?
    ) = Unit

    // Never called for full backup.
    final override fun onRestore(
        data: BackupDataInput?,
        appVersionCode: Int,
        newState: ParcelFileDescriptor?
    ) = Unit
}
