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
package com.infomaniak.core.common

import android.os.DeadObjectException
import android.os.Message
import android.os.Messenger

inline fun Messenger.trySending(configureMessage: (Message) -> Unit): Boolean {
    val reply = Message.obtain().also { msg ->
        msg.isAsynchronous = true
        configureMessage(msg)
    }
    return try {
        send(reply)
        true
    } catch (_: DeadObjectException) {
        // The client app died before we could respond. Usually, we can safely ignore it.
        false
    }
}
