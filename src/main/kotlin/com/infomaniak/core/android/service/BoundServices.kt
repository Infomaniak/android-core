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
suspend fun <R> Context.withBoundService(
    service: Intent,
    timeoutConnection: suspend (isWaitingForReconnect: Boolean) -> Unit,
    onDisconnected: () -> OnServiceDisconnectionBehavior<R>,
    onBindingIssue: suspend (issue: ServiceBindingIssue) -> OnBindingIssue<R>,
    flags: Int = Context.BIND_AUTO_CREATE,
    block: suspend (serviceBinder: IBinder) -> R,
): R {
    val connection = ServiceConnection(onDisconnected)
    bindLoop@ while (true) {
        try {
            // First, we try to bind the service
            val bindingIssue: ServiceBindingIssue.NotFoundOrNoPermission? = withContext(NonCancellable + Dispatchers.IO) {
                try {
                    val serviceBindingStarted = bindService(service, connection, flags)
                    // The binding attempt can fail early.
                    if (serviceBindingStarted) null else ServiceBindingIssue.NotFoundOrNoPermission(null)
                } catch (e: SecurityException) {
                    ServiceBindingIssue.NotFoundOrNoPermission(e)
                }
            }

            if (bindingIssue != null) when (val bindingIssueBehavior = onBindingIssue(bindingIssue)) {
                is OnBindingIssue.GiveUp -> return bindingIssueBehavior.returnValue
                OnBindingIssue.UnbindAndRetry -> continue@bindLoop
            }

            var isWaitingForReconnect = false
            reconnectLoop@ while (true) {
                // Then, we await for connection (or any issue)
                val bindingOrIssue: Xor<IBinder, ServiceBindingIssue> = raceOf(
                    { Xor.First(connection.awaitConnect()) },
                    { Xor.Second(connection.awaitBindingIssue()) },
                    {
                        // Sometimes, onServiceConnected is never called.
                        // This happens often when the app has just been updated, so we use a timeout.
                        timeoutConnection(isWaitingForReconnect)
                        Xor.Second(ServiceBindingIssue.Timeout)
                    }
                )

                // Get the binder interface, if connected successfully, or jump out if there was any issue.
                val binding: IBinder = when (bindingOrIssue) {
                    is Xor.First -> bindingOrIssue.value
                    is Xor.Second -> when (val bindingIssueBehavior = onBindingIssue(bindingOrIssue.value)) {
                        is OnBindingIssue.GiveUp -> return bindingIssueBehavior.returnValue
                        OnBindingIssue.UnbindAndRetry -> continue@bindLoop
                    }
                }
                // Run the block with the binder, while listening for disconnection, and binding death.
                val result: Xor<R, ServiceBindingIssue?> = raceOf(
                    { Xor.First(block(binding)) },
                    {
                        Dispatchers.IO { binding.awaitProcessDeath() }
                        Xor.Second(ServiceBindingIssue.BindingDied)
                    },
                    {
                        when (val onDisconnectBehavior = connection.awaitDisconnect()) {
                            is OnServiceDisconnectionBehavior.UnbindImmediately -> Xor.First(onDisconnectBehavior.returnValue)
                            is OnServiceDisconnectionBehavior.AwaitRebind -> raceOf(
                                { Xor.First(onDisconnectBehavior.awaitUnbind()) },
                                {
                                    connection.awaitReconnect()
                                    Xor.Second(null)
                                },
                            )
                        }
                    },
                )

                // Return the result, wait for reconnection, or retry.
                return when (result) {
                    is Xor.First -> result.value
                    is Xor.Second -> if (result.value == null) {
                        isWaitingForReconnect = true
                        continue@reconnectLoop
                    } else when (val behavior = onBindingIssue(result.value)) {
                        is OnBindingIssue.GiveUp -> behavior.returnValue
                        OnBindingIssue.UnbindAndRetry -> continue@bindLoop
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
    } catch (_: RemoteException) {
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

sealed interface ServiceBindingIssue {
    /** Even if [e] is null, it might still be a missing permission issue that led [Context.bindService] to return false. */
    data class NotFoundOrNoPermission(val e: SecurityException?) : ServiceBindingIssue
    data object BindingDied : ServiceBindingIssue
    data object NullBinding : ServiceBindingIssue
    data object Timeout : ServiceBindingIssue
}
