/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core.crossapplogin.back.internal.deviceinfo

import android.os.Build.VERSION.SDK_INT
import androidx.core.util.AtomicFile
import com.infomaniak.core.allConcurrent
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.crossapplogin.back.CrossAppLogin
import com.infomaniak.core.extensions.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.invoke
import splitties.coroutines.suspendBlockingLazy
import splitties.experimental.ExperimentalSplittiesApi
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

    data class AppVersions(
        val versionName: String?,
        val versionCode: Long,
    )

    val currentAppVersions = suspendBlockingLazy(Dispatchers.IO) {
        appCtx.packageManager.getPackageInfo(appCtx.packageName, 0).let {
            val versionCode = when {
                SDK_INT >= 28 -> it.longVersionCode
                else -> @Suppress("Deprecation") it.versionCode.toLong()
            }
            AppVersions(versionName = it.versionName, versionCode = versionCode)
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
            val currentAppVersion = currentAppVersions().versionCode
            currentAppVersion == lastSyncedAppVersion && lastSyncedUuid == crossAppDeviceId
        } catch (_: FileNotFoundException) {
            false
        }
    }

    @Throws(IOException::class)
    suspend fun updateLastSyncedKey(crossAppDeviceId: Uuid, userId: Long) = Dispatchers.IO {
        val lastSyncedKeyFile = lastSyncKeyFileForUser(userId)
        val currentAppVersion = currentAppVersions().versionCode
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

    suspend inline fun <reified T : AbstractDeviceInfoUpdateWorker> scheduleWorkerOnDeviceInfoUpdate(): Nothing =
        Dispatchers.Default {
            val crossAppLogin = CrossAppLogin.Companion.forContext(context = appCtx, coroutineScope = this)
            crossAppLogin.sharedDeviceIdFlow.collectLatest { currentCrossAppDeviceId ->
                val userIdsFlow = UserDatabase().userDao().allUsers.map { users -> users.map { it.id } }.distinctUntilChanged()
                userIdsFlow.collect { userIds ->
                    val isEverythingUpToDate = userIds.allConcurrent { isUpToDate(currentCrossAppDeviceId, it.toLong()) }
                    if (isEverythingUpToDate) return@collect
                    AbstractDeviceInfoUpdateWorker.schedule<T>()
                }
            }
            awaitCancellation()
        }

    private fun lastSyncKeyFileForUser(userId: Long): AtomicFile = AtomicFile(lastSyncedKeyDir.resolve("$userId"))
}
