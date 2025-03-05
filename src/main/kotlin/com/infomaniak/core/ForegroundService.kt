/*
 * Infomaniak SwissTransfer - Android
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

@file:Suppress("ObsoleteSdkInt")
@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core

import android.app.Notification
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build.VERSION.SDK_INT
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import splitties.bitflags.hasFlag
import splitties.bundle.BundleSpec
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import splitties.init.appCtx
import splitties.intents.ServiceIntentSpec
import splitties.intents.intent
import splitties.mainhandler.mainHandler
import splitties.mainthread.isMainThread
import splitties.systemservices.notificationManager

/**
 * Usage:
 * ```
 * class MyForegroundService : ForegroundService(Companion) {
 *
 *     companion object : ForegroundService.Companion.NoExtras<MyForegroundService>(
 *         intentSpec = serviceWithoutExtrasSpec<MyForegroundService>(),
 *         notificationId = MyNotificationController.Ids.MY_FG_SERVICE
 *     )
 *
 *     override suspend fun run() {
 *         TODO("Call startForeground with a notification and run a coroutine.")
 *     }
 *
 *     override fun getStartNotification(intent: Intent, isReDelivery: Boolean): Notification {
 *         TODO("Return the initial notification")
 *     }
 * }
 * ```
 */
abstract class ForegroundService(
    private val companion: Companion<*, *>,
    private val redeliverIntentIfKilled: Boolean
) : LifecycleService() {

    @Retention(AnnotationRetention.BINARY)
    @RequiresOptIn(level = RequiresOptIn.Level.ERROR)
    private annotation class InternalApi

    private val timeoutAsync = CompletableDeferred<TimeoutCancellationException>()
    private var foregroundStarted = false

    /**
     * `stopSelf()` is automatically called on this Service once this function finishes its
     * execution.
     */
    open suspend fun run() {
        awaitCancellation()
    }

    /**
     * Called when [onStartCommand] is called.
     * This function must return the id and the notification that will be used to call [startForeground].
     */
    protected abstract fun getStartNotification(intent: Intent, isReDelivery: Boolean): Notification

    @OptIn(InternalApi::class)
    fun updateNotification(notification: Notification) {
        if (isMainThread.not()) {
            mainHandler.post { updateNotification(notification) }
            return
        }
        if (foregroundStarted) {
            notificationManager.notify(companion.notificationId, notification)
        } else {
            startForegroundWithType(companion.notificationId, notification)
            foregroundStarted = true
        }
    }

    /**
     * Return another constant from the [ServiceInfo] class if you don't want to default to the one
     * provided in the `AndroidManifest.xml` file.
     */
    @RequiresApi(29)
    protected open fun foregroundServiceType(): Int = ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST

    class TimeoutCancellationException(
        val fgsType: Int
    ) : CancellationException("timeout reached for foreground service type: $fgsType")

    @CallSuper // Not final to allow Dagger/Hilt to be used by subclasses, if needed.
    override fun onCreate() {
        @OptIn(InternalApi::class)
        companion.markCreated()
        super.onCreate()
        lifecycleScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                val job = launch { run() }
                raceOf(
                    { job.join() },
                    { job.cancel(timeoutAsync.await()) }
                )
            } finally {
                stopSelf()
            }
        }
    }

    @RequiresApi(35)
    @CallSuper
    override fun onTimeout(startId: Int, fgsType: Int) {
        timeoutAsync.complete(TimeoutCancellationException(fgsType))
    }

    @RequiresApi(34)
    @CallSuper
    override fun onTimeout(startId: Int) {
        timeoutAsync.complete(TimeoutCancellationException(foregroundServiceType))
    }


    @OptIn(InternalApi::class)
    final override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        requireNotNull(intent) {
            "Intent should never be null when using Service.START_REDELIVER_INTENT or Service.START_NOT_STICKY"
        }
        if (foregroundStarted.not()) {
            val notification = getStartNotification(
                intent = intent,
                isReDelivery = flags.hasFlag(START_FLAG_REDELIVERY)
            )
            startForegroundWithType(companion.notificationId, notification)
            foregroundStarted = true
        }
        return if (redeliverIntentIfKilled) START_REDELIVER_INTENT else START_NOT_STICKY
    }

    private fun startForegroundWithType(notificationId: Int, notification: Notification) {
        if (SDK_INT >= 29) {
            startForeground(notificationId, notification, foregroundServiceType())
        } else {
            startForeground(notificationId, notification)
        }
    }

    final override fun onDestroy() {
        @OptIn(InternalApi::class)
        companion.markDestroyed()
        if (SDK_INT >= 24) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("deprecation")
            stopForeground(true)
        }
        super.onDestroy()
    }

    final override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    abstract class Companion<S : ForegroundService, ExtrasSpec : BundleSpec>(
        private val intentSpec: ServiceIntentSpec<S, ExtrasSpec>,
        @property:InternalApi val notificationId: Int
    ) {

        val isRunningFlow: StateFlow<Boolean>

        abstract class NoExtras<S : ForegroundService>(
            intentSpec: ServiceIntentSpec<S, Nothing>,
            notificationId: Int
        ) : Companion<S, Nothing>(intentSpec, notificationId) {

            suspend fun runUntilCancelled(): Nothing = try {
                start()
                awaitCancellation()
            } finally {
                stop()
            }

            @CallSuper
            open fun start() = ContextCompat.startForegroundService(appCtx, intent)

            private val intent: Intent = intentSpec.intent()
        }

        @CallSuper
        open fun start(configIntent: Intent.(ServiceIntentSpec<S, ExtrasSpec>, ExtrasSpec) -> Unit) {
            ContextCompat.startForegroundService(appCtx, intentSpec.intent(configIntent))
        }

        @CallSuper
        open fun stop() = appCtx.stopService(intent)

        @InternalApi // Discourage usage from same module.
        internal fun markCreated() {
            isRunningMutableStateFlow.value = true
        }

        @InternalApi // Discourage usage from same module.
        internal fun markDestroyed() {
            isRunningMutableStateFlow.value = false
        }

        private val intent: Intent = intentSpec.intent()
        private val isRunningMutableStateFlow = MutableStateFlow(false).also {
            isRunningFlow = it.asStateFlow()
        }
    }
}
