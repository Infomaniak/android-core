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
package com.infomaniak.core.auth.backup

import android.app.backup.FullBackupDataOutput
import com.google.android.gms.auth.blockstore.Blockstore
import com.google.android.gms.auth.blockstore.DeleteBytesRequest
import com.google.android.gms.auth.blockstore.RetrieveBytesRequest
import com.google.android.gms.auth.blockstore.StoreBytesData
import com.infomaniak.core.common.backup.FullBackupAgent
import kotlinx.coroutines.tasks.await
import splitties.init.appCtx

internal class BlockStorePlayServicesImpl : BlockStore() {
    private val client = Blockstore.getClient(appCtx)

    override suspend fun storeBytes(
        key: String,
        shouldBackupToCloud: Boolean,
        bytes: ByteArray
    ) {
        val storeRequest = StoreBytesData.Builder()
            .setKey(key)
            .setShouldBackupToCloud(shouldBackupToCloud)
            .setBytes(bytes)
            .build()
        client.storeBytes(storeRequest).await()
    }

    override suspend fun retrieveBytes(keys: List<String>?): Map<String, ByteArray> {
        val retrieveRequest = RetrieveBytesRequest.Builder()
            .setKeys(keys ?: emptyList())
            .setRetrieveAll(keys == null)
            .build()
        return client.retrieveBytes(retrieveRequest).await().blockstoreDataMap.mapValues { it.value.bytes }
    }

    override suspend fun deleteBytes(keys: List<String>): Boolean {
        val deleteRequest = DeleteBytesRequest.Builder().setKeys(keys).build()
        return client.deleteBytes(deleteRequest).await()
    }

    override suspend fun isE2eeAvailable(): Boolean = client.isEndToEndEncryptionAvailable.await()

    context(agent: FullBackupAgent)
    override fun backupTestBlockStoreIfNeeded(data: FullBackupDataOutput) = Unit
}
