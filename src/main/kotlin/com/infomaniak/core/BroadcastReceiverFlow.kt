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
package com.infomaniak.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION.SDK_INT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import splitties.bitflags.withFlag
import splitties.init.appCtx

fun broadcastReceiverFlow(
    action: String,
    priority: Int = 999, // This is the max for non system apps.
    emitInitialEmptyIntent: Boolean = false,
    exported: Boolean = SDK_INT < 33,
    visibleToInstantApps: Boolean = false
): Flow<Intent> = broadcastReceiverFlow(
    filter = IntentFilter(action).also { it.priority = priority },
    emitInitialEmptyIntent = emitInitialEmptyIntent,
    exported = exported,
    visibleToInstantApps = visibleToInstantApps
)

fun broadcastReceiverFlow(
    filter: IntentFilter,
    emitInitialEmptyIntent: Boolean = false,
    exported: Boolean = SDK_INT < 33,
    visibleToInstantApps: Boolean = false
): Flow<Intent> {
    if (visibleToInstantApps) require(exported)
    return broadcastReceiverFlowUnchecked(
        filter = filter,
        emitInitialEmptyIntent = emitInitialEmptyIntent,
        exported = exported,
        visibleToInstantApps = visibleToInstantApps
    )
}

private fun broadcastReceiverFlowUnchecked(
    filter: IntentFilter,
    emitInitialEmptyIntent: Boolean,
    exported: Boolean,
    visibleToInstantApps: Boolean
): Flow<Intent> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(intent)
        }
    }
    val ctx = appCtx
    val flag = when {
        SDK_INT >= 33 -> if (exported) Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED
        else -> 0
    }.let {
        if (visibleToInstantApps) it.withFlag(Context.RECEIVER_VISIBLE_TO_INSTANT_APPS) else it
    }
    ctx.registerReceiver(receiver, filter, flag)
    if (emitInitialEmptyIntent) trySend(Intent())
    awaitClose {
        ctx.unregisterReceiver(receiver)
    }
}.flowOn(Dispatchers.IO)
