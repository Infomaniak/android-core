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
package com.infomaniak.core.twofactorauth.back.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.AudioAttributes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.infomaniak.core.notifications.notifyCompat
import com.infomaniak.core.twofactorauth.back.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.invoke
import splitties.init.appCtx
import kotlin.LazyThreadSafetyMode.NONE

object TwoFactorAuthNotifications {

    const val TYPE = "2fa:push_challenge:approval"

    const val TOPIC = "2fa:push_challenge"

    private const val CHANNEL_ID = "2fa-challenge"
    private const val NOTIFICATION_ID = 0

    private const val NOTIFICATION_TAG_PREFIX = "2fa-challenge-"

    private val notificationManager by lazy(NONE) { NotificationManagerCompat.from(appCtx) }

    private val isAppCurrentlyVisible by ProcessLifecycleOwner.get().lifecycle.currentStateFlow.map {
        it.isAtLeast(Lifecycle.State.STARTED)
    }.distinctUntilChanged().onEach { isAppVisible ->
        if (isAppVisible) clearAllChallengeNotifications()
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = false
    )::value

    fun channel() = NotificationChannel(
        CHANNEL_ID,
        appCtx.getString(R.string.twoFactorAuthNotificationChannelName),
        NotificationManager.IMPORTANCE_HIGH,
    ).also {
        it.description = appCtx.getString(R.string.twoFactorAuthNotificationChannelDescription)
        it.enableLights(true)
        it.enableVibration(true)
        it.setSound(null, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
    }

    fun postIncomingChallengeNotification(userId: Long?, expirationTimeInMillis: Long) {
        if (isAppCurrentlyVisible) return // Already popped-up in the app.
        notificationManager.notifyCompat(
            tag = notificationTagFor(userId),
            notificationId = NOTIFICATION_ID,
            notification = createIncomingChallengeNotification(expirationTimeInMillis)
        )
    }

    private suspend fun clearAllChallengeNotifications() {
        Dispatchers.IO {
            notificationManager.activeNotifications.forEach {
                if (it.id == NOTIFICATION_ID && it.tag.startsWith(NOTIFICATION_TAG_PREFIX)) {
                    notificationManager.cancel(it.tag, it.id)
                }
            }
        }
    }

    private fun notificationTagFor(userId: Long?): String = "$NOTIFICATION_TAG_PREFIX$userId"

    private fun createIncomingChallengeNotification(expirationTimeInMillis: Long): Notification {
        val intent = appCtx.packageManager.getLaunchIntentForPackage(appCtx.packageName)!! // Never null for our use case, see doc
        val pendingIntent = PendingIntent.getActivity(
            /* context = */ appCtx,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val timeoutMillis = expirationTimeInMillis - System.currentTimeMillis()
        return NotificationCompat.Builder(appCtx, CHANNEL_ID)
            .setSmallIcon(R.drawable.shield_k)
            .setContentTitle(appCtx.getString(R.string.twoFactorAuthNotificationApproval))
            .setContentIntent(pendingIntent)
            .setTimeoutAfter(timeoutMillis)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setSound(null)
            .build()
    }
}
