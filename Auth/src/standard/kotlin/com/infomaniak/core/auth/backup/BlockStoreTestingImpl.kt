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
@file:OptIn(ExperimentalAtomicApi::class)

package com.infomaniak.core.auth.backup

import android.app.backup.FullBackupDataOutput
import com.infomaniak.core.common.backup.FullBackupAgent
import com.infomaniak.core.common.backup.isDeviceToDeviceTransfer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import splitties.init.appCtx
import java.io.File
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal class BlockStoreTestingImpl(private val e2eeAvailable: Boolean) : BlockStore() {

    private val dir by lazy { appCtx.filesDir.resolve("debugBlockStore").also { it.mkdir() } }
    private val shouldBackupToCloudSuffix = "-cloud-ok"

    override suspend fun storeBytes(key: String, shouldBackupToCloud: Boolean, bytes: ByteArray) {
        Dispatchers.IO {
            val cloudOkFile = dir.resolve(key + shouldBackupToCloudSuffix)
            val deviceToDeviceOnlyFile = dir.resolve(key)
            val file = if (shouldBackupToCloud) cloudOkFile else deviceToDeviceOnlyFile
            file.writeBytes(bytes)
            val fileToDelete = if (shouldBackupToCloud) deviceToDeviceOnlyFile else cloudOkFile
            fileToDelete.delete()
        }
    }

    override suspend fun retrieveBytes(keys: List<String>?): Map<String, ByteArray> = Dispatchers.IO {
        val targetKeys = keys ?: getStoredKeys()
        buildMap(capacity = targetKeys.size) {
            targetKeys.forEach { key ->
                val bytes = fileForKey(key)?.readBytes() ?: return@forEach
                this[key] = bytes
            }
        }
    }

    override suspend fun deleteBytes(keys: List<String>): Boolean = Dispatchers.IO {
        var deleted = false
        keys.forEach { key ->
            deleted = dir.resolve(key).delete() || deleted
            deleted = dir.resolve(key + shouldBackupToCloudSuffix).delete() || deleted
        }
        deleted
    }

    override suspend fun isE2eeAvailable(): Boolean = e2eeAvailable

    context(agent: FullBackupAgent)
    override fun backupTestBlockStoreIfNeeded(data: FullBackupDataOutput) {
        dir.listFiles()?.forEach { file ->
            if (data.isDeviceToDeviceTransfer || file.name.endsWith(shouldBackupToCloudSuffix)) {
                agent.fullBackupFile(file, data)
            }
        }
    }

    private fun fileForKey(key: String): File? {
        return dir.resolve(key).takeIf { it.exists() } ?: dir.resolve(key + shouldBackupToCloudSuffix).takeIf { it.exists() }
    }

    private fun getStoredKeys(): List<String> {
        return (dir.list()?.asList() ?: emptyList<String>()).map { it.removeSuffix(shouldBackupToCloudSuffix) }
    }
}
