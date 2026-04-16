/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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
@file:OptIn(ExperimentalContracts::class)

package com.infomaniak.core.crossapplogin.back.internal.deviceid

import android.provider.Settings
import android.provider.Settings.Secure.ANDROID_ID
import androidx.annotation.VisibleForTesting
import androidx.core.util.AtomicFile
import com.infomaniak.core.common.extensions.write
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdResync.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.invoke
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import splitties.init.appCtx
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSerializationApi::class)
@ExperimentalUuidApi
internal object SharedDeviceIdStorage {

    internal val writtenIdFlow: SharedFlow<Uuid>

    private val dataFile = AtomicFile(appCtx.filesDir.resolve("generatedSharedDeviceId"))

    private val localDataReadWriteMutex = Mutex()

    private val _writtenIdFlow = MutableSharedFlow<Uuid>(replay = 1).also {
        writtenIdFlow = it.asSharedFlow()
    }

    suspend fun cleanSyncStatusesOfUninstalledApps(installedApps: Set<String>) {
        update { currentData ->
            currentData ?: return
            if (currentData.packageNamesStatuses.any { (packageName, _) -> packageName !in installedApps }) {
                currentData.copy(
                    packageNamesStatuses = currentData.packageNamesStatuses.filter { (packageName, _) ->
                        packageName in installedApps
                    }
                )
            } else return
        }
    }

    @Throws(IOException::class)
    internal suspend fun readDeviceId(): Uuid? = Dispatchers.IO {
        localDataReadWriteMutex.withLock {
            readDataUnGuarded()?.let { sharedId -> Uuid.fromByteArray(sharedId.uuid) }
        }
    }

    suspend fun getPackagesWithStatus(expectedSharedDeviceId: Uuid, expectedStatus: SyncStatus): Set<String> {
        return readAll(expectedSharedDeviceId)?.packageNamesStatuses?.mapNotNullTo(mutableSetOf()) { (packageName, status) ->
            packageName.takeIf { expectedStatus == status }
        } ?: emptySet()
    }

    suspend fun getSyncStatusForApp(packageName: String, expectedSharedDeviceId: Uuid): SyncStatus? {
        return readAll(expectedSharedDeviceId)?.packageNamesStatuses?.get(packageName)
    }

    suspend fun updateDeviceId(externalRequest: SharedDeviceIdResync.ResyncRequest): Boolean {
        val didIdChange: Boolean
        update { currentData ->
            if (currentData == null) {
                didIdChange = true
                SharedDeviceId(
                    androidId = getAndroidId(),
                    uuid = externalRequest.id.toByteArray(),
                    syncRequesterPackageName = externalRequest.sourceAppPackageName,
                    packageNamesStatuses = mapOf(externalRequest.sourceAppPackageName to SyncStatus.SyncedExternally)
                )
            } else {
                val newId = externalRequest.id.toByteArray()
                didIdChange = !(currentData.uuid contentEquals newId)
                if (didIdChange) currentData.copy(
                    uuid = newId,
                    lastSyncRequesterPackageName = externalRequest.sourceAppPackageName,
                    packageNamesStatuses = mapOf(externalRequest.sourceAppPackageName to SyncStatus.SyncedExternally)
                ) else currentData.copy(
                    lastSyncRequesterPackageName = externalRequest.sourceAppPackageName,
                    packageNamesStatuses = currentData.packageNamesStatuses.let {
                        if (it[externalRequest.sourceAppPackageName] == SyncStatus.SyncedExternally) {
                            it
                        } else {
                            it + (externalRequest.sourceAppPackageName to SyncStatus.SyncedExternally)
                        }
                    }
                )
            }
        }
        return didIdChange
    }

    suspend fun updateSyncStatus(packageName: String, id: Uuid, status: SyncStatus) {
        update { currentData ->
            checkNotNull(currentData)
            check(id.toByteArray() contentEquals currentData.uuid)
            currentData.copy(packageNamesStatuses = currentData.packageNamesStatuses + (packageName to status))
        }
    }

    @Throws(IOException::class)
    internal suspend fun setDeviceId(sharedDeviceId: Uuid) {
        Dispatchers.IO {
            localDataReadWriteMutex.withLock {
                dataFile.write { outputStream ->
                    val data = SharedDeviceId(
                        androidId = getAndroidId(),
                        uuid = sharedDeviceId.toByteArray()
                    ).let { ProtoBuf.encodeToByteArray(it) }

                    outputStream.write(data)
                }
                _writtenIdFlow.tryEmit(sharedDeviceId)
            }
        }
    }

    @Throws(IOException::class)
    private suspend fun readAll(expectedSharedDeviceId: Uuid) = Dispatchers.IO {
        localDataReadWriteMutex.withLock { readDataUnGuarded() }?.takeIf {
            it.uuid contentEquals expectedSharedDeviceId.toByteArray()
        }
    }

    private suspend inline fun update(block: (SharedDeviceId?) -> SharedDeviceId) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        localDataReadWriteMutex.withLock {
            val currentValue: SharedDeviceId? = Dispatchers.IO { readDataUnGuarded() }
            val newValue = block(currentValue)
            Dispatchers.IO {
                dataFile.write { outputStream ->
                    val data = ProtoBuf.encodeToByteArray(newValue)
                    outputStream.write(data)
                }
                _writtenIdFlow.tryEmit(Uuid.fromByteArray(newValue.uuid))
            }
        }
    }

    private fun readDataUnGuarded(): SharedDeviceId? = try {
         dataFile.openRead().use { stream ->
            ProtoBuf.decodeFromByteArray<SharedDeviceId>(stream.readBytes()).takeUnless {
                it.androidId != getAndroidId()
                // If different, the value was restored from a backup of another device (or post-reset).
            }
        }
    } catch (_: FileNotFoundException) {
        null
    }

    /**
     * ## Important message to editors
     *
     * For all existing fields, unless the given field never made it to a public release (which includes betas!):
     * - **NEVER** change the type of an existing field.
     * - **NEVER** change the value in the `@ProtoNumber` of an existing field.
     * - **NEVER** remove a field that was present before.
     * - **NEVER** reuse a protobuf number. Those shall be unique and stable, essentially forever.
     * - **Safe changes:**
     *    1. Renaming any existing field.
     *    2. Adding new fields with explicit `@ProtoNumber` using a new, never used number.
     *    3. Deprecating any field (without touching its `@ProtoNumber`).
     *    4. Rename this data class.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Serializable
    internal class SharedDeviceId(
        @ProtoNumber(1) val androidId: String,
        @ProtoNumber(2) val uuid: ByteArray,
        @ProtoNumber(3) val syncRequesterPackageName: String? = null,
        @ProtoNumber(4) val packageNamesStatuses: Map<String, SyncStatus> = emptyMap(),
    ) {
        fun copy(
            androidId: String = this.androidId,
            uuid: ByteArray = this.uuid,
            lastSyncRequesterPackageName: String? = this.syncRequesterPackageName,
            packageNamesStatuses: Map<String, SyncStatus> = this.packageNamesStatuses
        ): SharedDeviceId = SharedDeviceId(
            androidId = androidId,
            uuid = uuid,
            syncRequesterPackageName = lastSyncRequesterPackageName,
            packageNamesStatuses = packageNamesStatuses
        )
    }

    private fun getAndroidId(): String {
        @Suppress("HardwareIds")
        return Settings.Secure.getString(appCtx.contentResolver, ANDROID_ID)
    }
}
