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
import android.os.IBinder
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.Channel
import android.content.ServiceConnection as AndroidServiceConnection

internal class ServiceConnection<R>(
    private val onDisconnected: () -> OnServiceDisconnectionBehavior<R>
) : AndroidServiceConnection {

    private val bindingIssueChannel = Channel<ServiceBindingIssue>(capacity = Channel.Factory.CONFLATED)
    private val binderChannel = Channel<IBinder>(capacity = Channel.Factory.CONFLATED)
    private val onDisconnectChannel = Channel<OnServiceDisconnectionBehavior<R>>(capacity = Channel.Factory.CONFLATED)

    suspend fun awaitBindingIssue(): ServiceBindingIssue = bindingIssueChannel.receive()

    suspend fun awaitConnect(): IBinder = binderChannel.receive()

    suspend fun awaitReconnect() {
        val newConnectionBinder = binderChannel.receive()
        binderChannel.send(newConnectionBinder) // Resend it so it can be received again in awaitBind()
    }

    suspend fun awaitDisconnect(): OnServiceDisconnectionBehavior<R> = onDisconnectChannel.receive()


    @RequiresApi(28)
    override fun onNullBinding(name: ComponentName) {
        bindingIssueChannel.trySend(ServiceBindingIssue.NullBinding)
    }

    @RequiresApi(26)
    override fun onBindingDied(name: ComponentName) {
        bindingIssueChannel.trySend(ServiceBindingIssue.BindingDied)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder?) {
        when (service) {
            null -> bindingIssueChannel.trySend(ServiceBindingIssue.NullBinding)
            else -> binderChannel.trySend(service)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        val onDisconnectedBehavior = onDisconnected()
        onDisconnectChannel.trySend(onDisconnectedBehavior)
    }
}
