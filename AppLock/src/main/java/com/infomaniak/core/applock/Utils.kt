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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.Display
import android.widget.CompoundButton
import androidx.activity.ComponentActivity
import androidx.annotation.ColorInt
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.eventFlow
import androidx.lifecycle.lifecycleScope
import com.infomaniak.core.applock.view.LockViewActivity.Companion.UNDEFINED_PRIMARY_COLOR
import com.infomaniak.core.applock.view.LockViewActivityArgs
import com.infomaniak.core.common.extensions.appName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import splitties.init.appCtx
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object Utils {

    const val APP_LOCK_TAG = "App lock"

    @SuppressLint("NewApi")
    fun FragmentActivity.requestCredentials(onSuccess: () -> Unit) {
        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.i(APP_LOCK_TAG, "onAuthenticationSucceeded")
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.i(APP_LOCK_TAG, "onAuthenticationFailed")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.i(APP_LOCK_TAG, "onAuthenticationError errorCode: $errorCode errString: $errString")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(appName)
            .setAllowedAuthenticators(LockManager.authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun FragmentActivity.silentlyReverseSwitch(switch: CompoundButton, onCredentialSuccessful: (Boolean) -> Unit) = with(switch) {
        if (tag == null) {
            silentClick()
            requestCredentials {
                silentClick() // Click that doesn't pass in listener
                onCredentialSuccessful(isChecked)
            }
        }
    }

    fun FragmentActivity.silentlyReverseSwitch(isChecked: Boolean, onCredentialSuccessful: (Boolean) -> Unit) {
        requestCredentials {
            onCredentialSuccessful(isChecked)
        }
    }

    private fun CompoundButton.silentClick() {
        tag = true
        performClick()
        tag = null
    }
}

object LockManager {
    val defaultAutoLockTimeout = 1.minutes

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
        lockActivityCls: Class<out BaseLockActivity>,
        isAppLockEnabled: suspend () -> Boolean,
        autoLockTimeout: Duration = defaultAutoLockTimeout,
        @ColorInt primaryColor: Int = UNDEFINED_PRIMARY_COLOR
    ) {
        targetActivity.lifecycleScope.launch {
            targetActivity.shouldEnableAppLockFlow(isAppLockEnabled).collectLatest { shouldLock ->
                if (shouldLock) lockAppWhenNeeded(
                    targetActivity = targetActivity,
                    lockActivityCls = lockActivityCls,
                    primaryColor = primaryColor,
                    autoLockTimeout = autoLockTimeout
                )
            }
        }
    }

    private suspend fun lockAppWhenNeeded(
        targetActivity: ComponentActivity,
        lockActivityCls: Class<out BaseLockActivity>,
        @ColorInt primaryColor: Int,
        autoLockTimeout: Duration
    ) = targetActivity.lifecycle.eventFlow.buffer(Channel.UNLIMITED).collect { event ->
        when (event) {
            ON_RESUME -> lockIfNeeded(
                targetActivity = targetActivity,
                lockActivityCls = lockActivityCls,
                lastAppClosingTime = lastAppClosingTime,
                primaryColor = primaryColor,
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
        lockActivityCls: Class<out BaseLockActivity>,
        lastAppClosingTime: Long,
        primaryColor: Int = UNDEFINED_PRIMARY_COLOR,
        autoLockTimeout: Duration
    ) {
        if (lockedByScreenTurnedOff) {
            lockNow(targetActivity, lockActivityCls, primaryColor)
        } else {
            val now = SystemClock.elapsedRealtime()
            val timeoutExceeded = now > lastAppClosingTime + autoLockTimeout.inWholeMilliseconds
            if (timeoutExceeded) {
                lockNow(targetActivity, lockActivityCls, primaryColor)
            }
        }
    }

    private fun lockNow(originalActivity: Activity, lockActivityCls: Class<out BaseLockActivity>, primaryColor: Int) {
        Log.v("Jamy", "lockNow: ${lockActivityCls.name}")
        Intent(originalActivity, lockActivityCls).apply {
            val args = LockViewActivityArgs(
                destinationClassName = originalActivity::class.java.name,
                primaryColor = primaryColor,
                shouldStartActivity = false,
                destinationClassArgs = null
            )
            putExtras(args.toBundle())
        }.also(originalActivity::startActivity)
        isLocked = true
    }

    fun hasBiometrics(): Boolean {
        return biometricsManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
