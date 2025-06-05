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
package com.infomaniak.core.android.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.annotation.experimental.UseExperimental
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import splitties.coroutines.raceOf

/**
 * Attempts to bind to the [service], runs [block] once connected, and cancels it on disconnection.
 * Finally unbinds the service.
 *
 * This function is cancellable. It is recommended to use a proper scope that cancels when the
 * connecting component is destroyed, or stopped.
 *
 * You may want to use this in a loop.
 */
@Throws(SecurityException::class)
suspend fun <R> Context.withBoundService(
    service: Intent,
    allowRebind: Boolean = true,
    bindTimeout: suspend () -> R,
    shouldRebind: suspend () -> OnServiceDisconnectionBehavior,
    onBindingIssue: suspend (issue: ServiceBindingIssue) -> R,
    flags: Int = Context.BIND_AUTO_CREATE,
    block: suspend (serviceBinder: IBinder) -> R
): R? {
    val binderChannel = Channel<IBinder?>(capacity = Channel.CONFLATED)

    val connection = object : ServiceConnection {

        @RequiresApi(28)
        override fun onNullBinding(name: ComponentName) {
            TODO("Send the issue")
        }

        @RequiresApi(26)
        override fun onBindingDied(name: ComponentName) {
            TODO("Send the issue")
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            binderChannel.trySend(service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            binderChannel.trySend(null)
            TODO("Do we want to handle it like that?")
        }
    }
    try {
        val serviceBindingStarted = bindService(service, connection, flags)
        return if (serviceBindingStarted) coroutineScope {
            binderChannel.consume {
                val timeoutResultOrBinder = raceOf({
                    // Sometimes, onServiceConnected is never called.
                    // This happens often when the app has just been updated, so we use a timeout.
                    //TODO: Mention this in the KDoc
                    bindTimeout()
                    TODO("Use a Xor/Either class")
                }, {
                    val serviceBinder = receive()
                    TODO("Use a Xor/Either class")
                })
                TODO("Finish that")
                val job = launch {
                    block(serviceBinder)
                }
                TODO("Do it like that if correct, or redo it")
                raceOf(
                    { receive(); /* Second value always comes from onServiceDisconnected. */job.cancel() },
                    { job.join() }
                )
            }
        } else onBindingIssue(ServiceBindingIssue.NotFoundOrNoPermission)
    } finally {
        unbindService(connection)
    }
}

sealed interface OnServiceDisconnectionBehavior {
    data object Await
}

enum class ServiceBindingIssue {
    NotFoundOrNoPermission,
    BindingDied,
    NullBinding,
}
