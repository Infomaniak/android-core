/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2026 Infomaniak Network SA
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
package com.infomaniak.core.applock

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.eventFlow
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import splitties.init.appCtx
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object AppLockManager {
    private val defaultAutoLockTimeout = 1.minutes

    private val biometricsManager = BiometricManager.from(appCtx)
    internal const val authenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    // On a cold start, do as if the app was left for more than the timeout.
    private var lastAppClosingTime = SystemClock.elapsedRealtime() - (defaultAutoLockTimeout.inWholeMilliseconds + 1)
    private var lockedByScreenTurnedOff = false
    private var isLocked = true

    private val scope = MainScope()

    init {
        scope.launch {
            DisplayState.stateForDisplay(Display.DEFAULT_DISPLAY).collect { state ->
                if (state != DisplayState.On) {
                    lockedByScreenTurnedOff = true
                }
            }
        }
    }

    /**
     * Meant to be used when enabling app lock, so the user doesn't experience a double lock.
     */
    fun unlock() {
        lockedByScreenTurnedOff = false
        lastAppClosingTime = SystemClock.elapsedRealtime() // Avoid locking again immediately
        isLocked = false
    }

    fun scheduleLockIfNeeded(
        targetActivity: ComponentActivity,
        lockActivityCls: KClass<out BaseAppLockActivity>,
        isAppLockEnabled: suspend () -> Boolean,
        autoLockTimeout: Duration = defaultAutoLockTimeout,
    ) {
        targetActivity.lifecycleScope.launch {
            targetActivity.shouldEnableAppLockFlow(isAppLockEnabled).collectLatest { shouldLock ->
                if (shouldLock) lockAppWhenNeeded(
                    targetActivity = targetActivity,
                    lockActivityCls = lockActivityCls,
                    autoLockTimeout = autoLockTimeout
                )
            }
        }
    }

    private suspend fun lockAppWhenNeeded(
        targetActivity: ComponentActivity,
        lockActivityCls: KClass<out BaseAppLockActivity>,
        autoLockTimeout: Duration
    ) = targetActivity.lifecycle.eventFlow.buffer(Channel.UNLIMITED).collect { event ->
        when (event) {
            ON_RESUME -> lockIfNeeded(
                targetActivity = targetActivity,
                lockActivityCls = lockActivityCls,
                lastAppClosingTime = lastAppClosingTime,
                autoLockTimeout = autoLockTimeout
            )
            ON_PAUSE -> if (!isLocked) lastAppClosingTime = SystemClock.elapsedRealtime()
            else -> Unit
        }
    }

    private fun ComponentActivity.shouldEnableAppLockFlow(
        isAppLockEnabled: suspend () -> Boolean
    ): Flow<Boolean> = lifecycle.currentStateFlow.transform {
        if (it == Lifecycle.State.RESUMED) {
            emit(hasBiometrics() && isAppLockEnabled())
        }
    }.distinctUntilChanged()

    private fun lockIfNeeded(
        targetActivity: Activity,
        lockActivityCls: KClass<out BaseAppLockActivity>,
        lastAppClosingTime: Long,
        autoLockTimeout: Duration
    ) {
        if (lockedByScreenTurnedOff) {
            lockNow(targetActivity, lockActivityCls)
        } else {
            val now = SystemClock.elapsedRealtime()
            val timeoutExceeded = now > lastAppClosingTime + autoLockTimeout.inWholeMilliseconds
            if (timeoutExceeded) {
                lockNow(targetActivity, lockActivityCls)
            }
        }
    }

    private fun lockNow(originalActivity: Activity, lockActivityCls: KClass<out BaseAppLockActivity>) {
        val intent = Intent(originalActivity, lockActivityCls.java)
        originalActivity.startActivity(intent)
        isLocked = true
    }

    fun hasBiometrics(): Boolean {
        return biometricsManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
