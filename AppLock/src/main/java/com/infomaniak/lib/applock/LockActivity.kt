/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2024 Infomaniak Network SA
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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.lifecycle.*
import androidx.lifecycle.Lifecycle.Event.*
import androidx.navigation.navArgs
import com.infomaniak.lib.applock.Utils.requestCredentials
import com.infomaniak.lib.applock.databinding.ActivityLockBinding
import com.infomaniak.lib.core.utils.getAppName
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import splitties.init.appCtx
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class LockActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLockBinding.inflate(layoutInflater) }
    private val lockViewModel: LockViewModel by viewModels()
    private val navigationArgs: LockActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) = with(binding) {
        super.onCreate(savedInstanceState)
        setContentView(root)
        if (lockViewModel.firstLaunch) {
            requestCredentials { onCredentialsSuccessful() }
        }

        onBackPressedDispatcher.addCallback(this@LockActivity) {
            finishAffinity()
            exitProcess(0)
        }

        unLock.apply {
            if (navigationArgs.primaryColor != UNDEFINED_PRIMARY_COLOR) setBackgroundColor(navigationArgs.primaryColor)
            setOnClickListener { requestCredentials { onCredentialsSuccessful() } }
        }
        imageViewTitle.contentDescription = getAppName()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lockViewModel.firstLaunch = false
    }

    // TODO: Fix deprecated
    override fun onPause() {
        super.onPause()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun onCredentialsSuccessful() = with(navigationArgs) {
        Log.i(Utils.APP_LOCK_TAG, "success")
        lockedByScreenTurnedOff = false
        lastAppClosingTime = SystemClock.elapsedRealtime() // Avoid locking again immediately
        isLocked = false
        if (shouldStartActivity) {
            Intent(this@LockActivity, Class.forName(destinationClassName)).apply {
                destinationClassArgs?.let(::putExtras)
            }.also(::startActivity)
        }
        finish()
    }

    class LockViewModel : ViewModel() {
        var firstLaunch = true
    }

    companion object {

        private const val UNDEFINED_PRIMARY_COLOR = 0
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

        fun scheduleLockIfNeeded(
            targetActivity: ComponentActivity,
            @ColorInt primaryColor: Int = UNDEFINED_PRIMARY_COLOR,
            autoLockTimeout: Duration = defaultAutoLockTimeout,
            isAppLockEnabled: () -> Boolean
        ) {
            targetActivity.lifecycleScope.launch {
                targetActivity.shouldEnableAppLockFlow(isAppLockEnabled).collectLatest { shouldLock ->
                    if (shouldLock) coroutineScope {
                        launch { keepAppClosingTime(targetActivity) }
                        lockAppWhenNeeded(
                            targetActivity = targetActivity,
                            primaryColor = primaryColor,
                            autoLockTimeout = autoLockTimeout
                        )
                    }
                }
            }
        }

        private suspend fun keepAppClosingTime(targetActivity: ComponentActivity) {
            targetActivity.lifecycle.eventFlow.collect { event ->
                if (isLocked) return@collect
                when (event) {
                    ON_RESUME, ON_STOP -> {
                        // If the app is left, or opened back, all while being unlocked,
                        // we mark it to calculate elapsed on going back.
                        lastAppClosingTime = SystemClock.elapsedRealtime()
                    }
                    else -> Unit
                }
            }
        }

        private suspend fun lockAppWhenNeeded(
            targetActivity: ComponentActivity,
            @ColorInt primaryColor: Int,
            autoLockTimeout: Duration
        ): Nothing = targetActivity.lifecycle.currentStateFlow.collect { state ->
            if (state == Lifecycle.State.RESUMED) lockIfNeeded(
                targetActivity = targetActivity,
                lastAppClosingTime = lastAppClosingTime,
                primaryColor = primaryColor,
                autoLockTimeout = autoLockTimeout
            )
        }

        private fun ComponentActivity.shouldEnableAppLockFlow(
            isAppLockEnabled: () -> Boolean
        ): Flow<Boolean> = lifecycle.currentStateFlow.transform {
            if (it == Lifecycle.State.RESUMED) {
                emit(hasBiometrics() && isAppLockEnabled())
            }
        }.distinctUntilChanged()

        private fun lockIfNeeded(
            targetActivity: Activity,
            lastAppClosingTime: Long,
            primaryColor: Int = UNDEFINED_PRIMARY_COLOR,
            autoLockTimeout: Duration
        ) {
            if (lockedByScreenTurnedOff) {
                lockNow(targetActivity, primaryColor)
            } else {
                val now = SystemClock.elapsedRealtime()
                val timeoutExceeded = now > lastAppClosingTime + autoLockTimeout.inWholeMilliseconds
                if (timeoutExceeded) {
                    lockNow(targetActivity, primaryColor)
                }
            }
        }

        private fun lockNow(originalActivity: Activity, primaryColor: Int) {
            Intent(originalActivity, LockActivity::class.java).apply {
                val args = LockActivityArgs(
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
}
