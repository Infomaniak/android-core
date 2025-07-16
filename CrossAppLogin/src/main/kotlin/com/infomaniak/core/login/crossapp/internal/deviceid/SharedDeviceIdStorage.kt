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
package com.infomaniak.core.login.crossapp.internal.deviceid

import android.provider.Settings
import android.provider.Settings.Secure.ANDROID_ID
import androidx.core.util.AtomicFile
import com.infomaniak.core.extensions.write
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

    @Throws(IOException::class)
    internal suspend fun readDeviceId(): Uuid? = Dispatchers.IO {
        localDataReadWriteMutex.withLock {
            try {
                dataFile.openRead().use { stream ->
                    ProtoBuf.decodeFromByteArray<SharedDeviceId>(stream.readBytes()).takeUnless {
                        it.androidId != getAndroidId()
                        // If different, the value was restored from a backup of another device (or post-reset).
                    }?.let { sharedId -> Uuid.fromByteArray(sharedId.uuid) }
                }
            } catch (_: FileNotFoundException) {
                null
            }
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

    /**
     * ## Important message to editors
     *
     * For all existing Unless the given field never made it to a public release (which includes betas!):
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
    @Serializable
    private class SharedDeviceId(
        @ProtoNumber(1) val androidId: String,
        @ProtoNumber(2) val uuid: ByteArray,
    )

    private suspend fun getAndroidId() = Dispatchers.IO {
        @Suppress("HardwareIds")
        Settings.Secure.getString(appCtx.contentResolver, ANDROID_ID)
    }
}
