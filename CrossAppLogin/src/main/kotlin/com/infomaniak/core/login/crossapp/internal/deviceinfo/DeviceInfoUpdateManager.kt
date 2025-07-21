/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
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
package com.infomaniak.core.login.crossapp.internal.deviceinfo

import android.os.Build.VERSION.SDK_INT
import androidx.core.util.AtomicFile
import androidx.work.WorkManager
import androidx.work.await
import com.infomaniak.core.allConcurrent
import com.infomaniak.core.extensions.write
import com.infomaniak.core.login.crossapp.CrossAppLogin
import com.infomaniak.lib.core.room.UserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.invoke
import splitties.init.appCtx
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeviceInfoUpdateManager private constructor() {

    companion object {
        val sharedInstance = DeviceInfoUpdateManager()
    }

    private val lastSyncedKeyDir = appCtx.filesDir.resolve("lastSyncedDeviceInfoKeys")

    private val currentAppVersion by lazy {
        appCtx.packageManager.getPackageInfo(appCtx.packageName, 0).let {
            when {
                SDK_INT >= 28 -> it.longVersionCode
                else -> @Suppress("Deprecation") it.versionCode.toLong()
            }
        }
    }

    @Throws(IOException::class)
    suspend fun isUpToDate(crossAppDeviceId: Uuid, userId: Long): Boolean = Dispatchers.IO {
        try {
            val lastSyncedUuid: Uuid
            val lastSyncedAppVersion: Long
            val lastSyncedKeyFile = lastSyncKeyFileForUser(userId)
            DataInputStream(lastSyncedKeyFile.openRead()).use { stream ->
                lastSyncedUuid = Uuid.fromLongs(
                    mostSignificantBits = stream.readLong(),
                    leastSignificantBits = stream.readLong()
                )
                lastSyncedAppVersion = stream.readLong()
            }
            currentAppVersion == lastSyncedAppVersion && lastSyncedUuid == crossAppDeviceId
        } catch (_: FileNotFoundException) {
            false
        }
    }

    @Throws(IOException::class)
    suspend fun updateLastSyncedKey(crossAppDeviceId: Uuid, userId: Long) = Dispatchers.IO {
        val lastSyncedKeyFile = lastSyncKeyFileForUser(userId)
        lastSyncedKeyFile.write { outputStream ->
            DataOutputStream(outputStream).use {
                crossAppDeviceId.toLongs { mostSignificantBits: Long, leastSignificantBits: Long ->
                    it.writeLong(mostSignificantBits)
                    it.writeLong(leastSignificantBits)
                    it.writeLong(currentAppVersion)
                }
            }
        }
    }

    suspend inline fun <reified T : AbstractDeviceInfoUpdateWorker> scheduleWorkerOnDeviceInfoUpdate(): Nothing = Dispatchers.Default {
        val crossAppLogin = CrossAppLogin.Companion.forContext(context = appCtx, coroutineScope = this)
        val workManager = WorkManager.getInstance(appCtx)
        workManager.cancelAllWork().await()
        workManager.pruneWork().await()
        crossAppLogin.sharedDeviceIdFlow.collectLatest { currentCrossAppDeviceId ->
            val userIdsFlow = UserDatabase().userDao().allUsers.map { users -> users.map { it.id } }.distinctUntilChanged()
            userIdsFlow.collect { userIds ->
                val everythingUpToDate = userIds.allConcurrent { userId -> isUpToDate(currentCrossAppDeviceId, userId.toLong()) }
                if (everythingUpToDate) return@collect
                AbstractDeviceInfoUpdateWorker.schedule<T>()
            }
        }
        awaitCancellation()
    }

    private fun lastSyncKeyFileForUser(userId: Long): AtomicFile = AtomicFile(lastSyncedKeyDir.resolve("$userId"))
}
