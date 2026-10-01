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
import androidx.collection.LongObjectMap
import androidx.collection.buildLongObjectMap
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.backup.FullBackupAgent
import com.infomaniak.core.common.cancellable
import com.infomaniak.core.sentry.SentryLog

object BlockStoreBackup {

    private const val TAG = "BlockStoreBackup"

    private val blockStore = BlockStore.instance
    private val db = UserDatabase.instance

    const val isSupported: Boolean = true

    context(agent: FullBackupAgent)
    fun backupTestBlockStoreIfNeeded(data: FullBackupDataOutput) {
        BlockStore.instance.backupTestBlockStoreIfNeeded(data)
    }

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
                return@count false // Didn't fail.
            }
            runCatching {
                blockStore.storeBytes(
                    key = userId.toString(),
                    shouldBackupToCloud = true,
                    bytes = accessToken.toByteArray()
                )
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
        blockStore.deleteBytes(keysToDelete)
    }

    private suspend fun dumpTokens(): LongObjectMap<String> = buildLongObjectMap {
        db.userDao().allUsers().forEach { user -> this[user.id.toLong()] = user.apiToken.accessToken }
    }

    private suspend fun readTokensBackup(): LongObjectMap<String>? {
        val dataMap = blockStore.retrieveBytes().ifEmpty { return null }
        return buildLongObjectMap {
            dataMap.forEach { (key, bytes) ->
                val userId = key.toLongOrNull() ?: return@forEach
                this[userId] = String(bytes)
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
}
