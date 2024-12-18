/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.lib.applock

import android.hardware.display.DisplayManager
import android.view.Display
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import splitties.mainhandler.mainHandler
import splitties.systemservices.displayManager

sealed interface DisplayState {
    data object Off : DisplayState
    data object Vr : DisplayState
    data object DozeSuspend : DisplayState
    data object Doze : DisplayState
    data object OnSuspend : DisplayState
    data object On : DisplayState
    data class Unknown(val value: Int) : DisplayState

    companion object {

        fun stateForDisplay(id: Int): Flow<DisplayState?> = callbackFlow {
            var display: Display? = null
            val listener = object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {
                    display = displayManager.getDisplay(id)
                    trySend(display?.state?.let { from(it) })
                }

                override fun onDisplayRemoved(displayId: Int) {
                    display = null
                    trySend(null)
                }

                override fun onDisplayChanged(displayId: Int) {
                    val state = display?.state?.let { from(it) }
                    trySend(state)
                }
            }
            displayManager.registerDisplayListener(listener, mainHandler)
            display = displayManager.getDisplay(id)
            trySend(display?.state?.let { from(it) })
            awaitClose { displayManager.unregisterDisplayListener(listener) }
        }.buffer(Channel.UNLIMITED)

        private fun from(state: Int): DisplayState = when (state) {
            Display.STATE_OFF -> Off
            Display.STATE_VR -> Vr
            Display.STATE_DOZE_SUSPEND -> DozeSuspend
            Display.STATE_DOZE -> Doze
            Display.STATE_ON_SUSPEND -> OnSuspend
            Display.STATE_ON -> On
            Display.STATE_UNKNOWN -> Unknown(state)
            else -> Unknown(state)
        }
    }
}