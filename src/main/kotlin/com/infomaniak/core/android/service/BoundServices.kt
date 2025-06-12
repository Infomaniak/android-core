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

package com.infomaniak.core.android.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import com.infomaniak.core.Xor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.invoke
import kotlinx.coroutines.withContext
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi

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
    timeoutConnection: suspend (isWaitingForReconnect: Boolean) -> Unit,
    onDisconnected: () -> OnServiceDisconnectionBehavior<R>,
    onBindingIssue: suspend (issue: ServiceBindingIssue) -> OnBindingIssue<R>,
    flags: Int = Context.BIND_AUTO_CREATE,
    block: suspend (serviceBinder: IBinder) -> R
): R {
    val connection = ChannelServiceConnection(onDisconnected)
    bindLoop@ while (true) {
        try {
            val serviceBindingStarted = withContext(NonCancellable + Dispatchers.IO) {
                bindService(service, connection, flags)
            }
            if (serviceBindingStarted.not()) {
                val bindingIssueBehavior = onBindingIssue(ServiceBindingIssue.NotFoundOrNoPermission)
                when (bindingIssueBehavior) {
                    is OnBindingIssue.GiveUp -> return bindingIssueBehavior.returnValue
                    OnBindingIssue.UnbindAndRetry -> continue@bindLoop
                }
            }
            var isWaitingForReconnect = false
            reconnectLoop@ while (true) {
                val bindingOrIssue: Xor<IBinder, ServiceBindingIssue> = raceOf({
                    Xor.First(connection.awaitConnect())
                }, {
                    // Sometimes, onServiceConnected is never called.
                    // This happens often when the app has just been updated, so we use a timeout.
                    timeoutConnection(isWaitingForReconnect)
                    Xor.Second(ServiceBindingIssue.Timeout)
                }, {
                    val bindingIssue = connection.awaitBindingIssue()
                    Xor.Second(bindingIssue)
                })

                val binding: IBinder = when (bindingOrIssue) {
                    is Xor.First -> bindingOrIssue.value
                    is Xor.Second -> when (val bindingIssueBehavior = onBindingIssue(bindingOrIssue.value)) {
                        is OnBindingIssue.GiveUp -> return bindingIssueBehavior.returnValue
                        OnBindingIssue.UnbindAndRetry -> continue@bindLoop
                    }
                }
                val result: Xor<R, ServiceBindingIssue?> = raceOf({
                    Xor.First(block(binding))
                }, {
                    Dispatchers.IO { binding.awaitProcessDeath() }
                    Xor.Second(ServiceBindingIssue.BindingDied)
                }, {
                    when (val onDisconnectBehavior = connection.awaitDisconnect()) {
                        is OnServiceDisconnectionBehavior.AwaitRebind -> raceOf({
                            val result = onDisconnectBehavior.awaitUnbind()
                            Xor.First(result)
                        }, {
                            connection.awaitReconnect()
                            Xor.Second(null)
                        })
                        is OnServiceDisconnectionBehavior.UnbindImmediately -> {
                            Xor.First(onDisconnectBehavior.returnValue)
                        }
                    }
                })
                return when (result) {
                    is Xor.First -> result.value
                    is Xor.Second -> when (result.value) {
                        null -> {
                            isWaitingForReconnect = true
                            continue@reconnectLoop
                        }
                        else -> when (val behavior = onBindingIssue(result.value)) {
                            is OnBindingIssue.GiveUp -> behavior.returnValue
                            OnBindingIssue.UnbindAndRetry -> continue@bindLoop
                        }
                    }
                }
            }
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                unbindService(connection)
                unbindService(connection) // Try to see if it's idempotent. //TODO: Remove this once we know.
            }
        }
    }
}

private suspend fun IBinder.awaitProcessDeath() {
    val bindingDiedCompletable = Job()
    val deathRecipient = IBinder.DeathRecipient { bindingDiedCompletable.complete() }
    try {
        linkToDeath(deathRecipient, 0)
        bindingDiedCompletable.join()
    }  catch (_: RemoteException) {
        // Already died
    } finally {
        unlinkToDeath(deathRecipient, 0)
    }
}

sealed interface OnServiceDisconnectionBehavior<R> {
    data class AwaitRebind<R>(internal val awaitUnbind: suspend () -> R) : OnServiceDisconnectionBehavior<R>
    data class UnbindImmediately<R>(internal val returnValue: R) : OnServiceDisconnectionBehavior<R>
}

sealed interface OnBindingIssue<R> {
    data object UnbindAndRetry : OnBindingIssue<Nothing>
    data class GiveUp<R>(internal val returnValue: R) : OnBindingIssue<R>
}

enum class ServiceBindingIssue {
    NotFoundOrNoPermission,
    BindingDied,
    NullBinding,
    Timeout,
}
