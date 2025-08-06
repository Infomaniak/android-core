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
package com.infomaniak.core.crossapplogin.back

import android.os.Bundle
import android.os.Message

/**
 * Retrieves a byte array in this message's [Message.obj], previously put with [putBundleWrappedDataInObj],
 * possibly (likely) in another app or process connected to this one.
 */
internal fun Message.unwrapByteArrayOrNull(): ByteArray? {
    return (obj as Bundle).getByteArray("")
}

/**
 * Puts a byte array inside this message's [Message.obj], to be later retrieved in another app or process
 * with [unwrapByteArrayOrNull].
 */
internal fun Message.putBundleWrappedDataInObj(data: ByteArray) {
    obj = Bundle().also { it.putByteArray("", data) }
}
