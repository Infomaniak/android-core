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
import splitties.init.appCtx

/**
 * Example usage:
 * ```
 * val isSomethingEnabled: Flow<Boolean> = broadcastReceiverFlow(
 *     action = Whatever.ACTION_SOMETHING_STATE_CHANGE,
 *     emitInitialEmptyIntent = true
 * ).map { intent ->
 *     if (intent.hasExtra(Whatever.EXTRA_SOMETHING_ENABLED)) {
 *         intent.getBooleanExtra(Whatever.EXTRA_SOMETHING_ENABLED, false)
 *     } else {
 *         Whatever.isSomethingEnabled() // Get current value when getting the empty intent.
 *     }
 * }
 * ```
 *
 * @param action used for the [IntentFilter]. Pass an [IntentFilter] to the overload if you need a more complex version.
 * @param priority between -999 and 999. See [IntentFilter.setPriority].
 * @param emitInitialEmptyIntent if enabled, will emit an empty `Intent` after registration, which can be detected to
 * check the current value.
 * @param exported Only needed for cross-app usage. See [Context.RECEIVER_EXPORTED] and [Context.RECEIVER_NOT_EXPORTED].
 */
fun broadcastReceiverFlow(
    action: String,
    priority: Int = 999, // This is the max for non system apps.
    emitInitialEmptyIntent: Boolean = false,
    exported: Boolean = SDK_INT < 33,
): Flow<Intent> = broadcastReceiverFlow(
    filter = IntentFilter(action).also { it.priority = priority },
    emitInitialEmptyIntent = emitInitialEmptyIntent,
    exported = exported,
)

/**
 * This overload that takes an [IntentFilter] is for advanced usages where specifying one action and the priority isn't enough.
 *
 * @param emitInitialEmptyIntent if enabled, will emit an empty `Intent` after registration, which can be detected to
 * check the current value.
 * @param exported Only needed for cross-app usage. See [Context.RECEIVER_EXPORTED] and [Context.RECEIVER_NOT_EXPORTED].
 */
fun broadcastReceiverFlow(
    filter: IntentFilter,
    emitInitialEmptyIntent: Boolean = false,
    exported: Boolean = SDK_INT < 33,
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
    }
    ctx.registerReceiver(receiver, filter, flag)
    if (emitInitialEmptyIntent) trySend(Intent())
    awaitClose {
        ctx.unregisterReceiver(receiver)
    }
}.flowOn(Dispatchers.IO)
