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

import androidx.collection.LongObjectMap
import androidx.collection.buildLongObjectMap
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.google.android.gms.auth.blockstore.Blockstore
import com.google.android.gms.auth.blockstore.DeleteBytesRequest
import com.google.android.gms.auth.blockstore.RetrieveBytesRequest
import com.google.android.gms.auth.blockstore.StoreBytesData
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.cancellable
import com.infomaniak.core.sentry.SentryLog
import kotlinx.coroutines.tasks.await
import splitties.init.appCtx

object BlockStoreBackup {

    private const val TAG = "BlockStoreBackup"

    private val blockstoreClient = Blockstore.getClient(appCtx)
    private val db = UserDatabase.instance

    const val isSupported: Boolean = true

    suspend fun backupTokens(): Boolean {
        val backupContent = dumpTokens()
        val alreadyBackedUpContent = readTokensBackup()
        if (backupContent == alreadyBackedUpContent) return true
        deleteObsoleteKeys(previousDump = alreadyBackedUpContent, tokensDump = backupContent)
        return writeTokensBackup(backupContent)
    }

    suspend fun restoreTokens(): Boolean {
        val passKeysBackup = readTokensBackup() ?: return false
        applyTokensBackup(passKeysBackup)
        return true
    }

    private suspend fun writeTokensBackup(tokensDump: LongObjectMap<String>): Boolean {
        val failures = tokensDump.count { userId, accessToken ->
            if (accessToken.isEmpty()) {
                // Ensure we don't overwrite with an empty token.
                // This can happen if a previous backup was aborted, leaving tokens in the Block Store, but out of the DB.
                return false // Didn't fail.
            }
            val storeRequest = StoreBytesData.Builder()
                .setKey(userId.toString())
                .setShouldBackupToCloud(true)
                .setBytes(accessToken.toByteArray())
                .build()
            runCatching {
                blockstoreClient.storeBytes(storeRequest).await()
                false // Didn't fail.
            }.cancellable().getOrElse { throwable ->
                SentryLog.wtf(TAG, "Failed to backup token", throwable)
                true // Failed.
            }
        }
        return failures == 0
    }

    private suspend fun deleteObsoleteKeys(
        previousDump: LongObjectMap<String>?,
        tokensDump: LongObjectMap<String>,
    ) {
       if (previousDump == null || previousDump.isEmpty()) return
        val keysToDelete: List<String> = buildList {
            previousDump.forEachKey { key -> if (key !in tokensDump) add(key.toString()) }
        }
        val deleteRequest = DeleteBytesRequest.Builder().setKeys(keysToDelete).build()
        blockstoreClient.deleteBytes(deleteRequest).await()
    }

    private suspend fun dumpTokens(): LongObjectMap<String> = buildLongObjectMap {
        db.userDao().allUsers().forEach { user -> this[user.id.toLong()] = user.apiToken.accessToken }
    }

    private suspend fun readTokensBackup(): LongObjectMap<String>? {
        val retrieveRequest = RetrieveBytesRequest.Builder()
            .setRetrieveAll(true)
            .build()
        val dataMap = blockstoreClient.retrieveBytes(retrieveRequest).await().blockstoreDataMap.ifEmpty { return null }
        return buildLongObjectMap {
            dataMap.forEach { (key, data) ->
                val userId = key.toLongOrNull() ?: return@forEach
                this[userId] = String(data.bytes)
            }
        }
    }

    private suspend fun applyTokensBackup(backup: LongObjectMap<String>) {
        db.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                backup.forEach { userId, accessToken ->
                    db.userDao().updateUserToken(userId = userId.toInt(), accessToken = accessToken)
                }
            }
        }
    }

    private suspend fun isE2eeAvailable(): Boolean = blockstoreClient.isEndToEndEncryptionAvailable.await()
}
