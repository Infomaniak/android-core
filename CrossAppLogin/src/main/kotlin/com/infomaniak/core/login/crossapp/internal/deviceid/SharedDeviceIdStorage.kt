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

import androidx.core.util.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import splitties.init.appCtx
import java.io.FileNotFoundException
import java.io.IOException

internal object SharedDeviceIdStorage {

    private val dataFile = AtomicFile(appCtx.filesDir.resolve("generatedSharedDeviceId.utf8"))

    private val localDataReadWriteMutex = Mutex()

    //TODO: Store ANDROID_ID or something to compare with current before returning,
    // so we never return a stale backed-up id.

    @Throws(IOException::class)
    suspend fun readDeviceId(): String? = Dispatchers.IO {
        localDataReadWriteMutex.withLock {
            try {
                dataFile.openRead().use { stream -> stream.reader().readText() }
            } catch (_: FileNotFoundException) {
                null
            }
        }
    }

    @Throws(IOException::class)
    suspend fun setDeviceId(sharedDeviceId: String) {
        Dispatchers.IO {
            localDataReadWriteMutex.withLock {
                //TODO: Check that we don't have it already, and report if we do?
                dataFile.startWrite().use { outputStream ->
                    try {
                        val writer = outputStream.writer()
                        writer.write(sharedDeviceId)
                        writer.flush()
                        dataFile.finishWrite(outputStream)
                    } catch (t: Throwable) {
                        dataFile.failWrite(outputStream)
                        throw t
                    }
                }
            }
        }
    }
}
