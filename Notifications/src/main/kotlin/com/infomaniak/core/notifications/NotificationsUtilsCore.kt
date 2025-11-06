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
package com.infomaniak.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.infomaniak.core.extensions.hasPermission
import com.infomaniak.core.extensions.hasPermissions
import splitties.init.appCtx

fun buildNotificationChannel(
    channelId: String,
    name: String,
    importance: Int,
    description: String? = null,
    groupId: String? = null,
): NotificationChannel {
    return NotificationChannel(channelId, name, importance).apply {
        description?.let { this.description = it }
        groupId?.let { group = it }
    }
}

fun Context.createNotificationChannels(
    channelList: List<NotificationChannel>,
    groupList: List<NotificationChannelGroup>? = null,
) {
    (getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager).apply {
        groupList?.let { createNotificationChannelGroups(it) }
        createNotificationChannels(channelList)
    }
}

fun Context.deleteNotificationChannels(channelList: List<String>) {
    (getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager).apply {
        channelList.forEach { deleteNotificationChannel(it) }
    }
}

fun Context.buildNotification(
    channelId: String,
    requestCode: Int,
    intent: Intent,
    icon: Int,
    @ColorInt iconColor: Int,
    title: String,
    description: String? = null,
    onlyAlertOnce: Boolean = false,
): NotificationCompat.Builder {
    val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    return NotificationCompat.Builder(this, channelId).apply {
        setTicker(title)
        setContentTitle(title)
        description?.let { setStyle(NotificationCompat.BigTextStyle().bigText(it)) }
        setSmallIcon(icon)
        setColor(iconColor)
        setAutoCancel(true)
        setOnlyAlertOnce(onlyAlertOnce)
        setContentIntent(PendingIntent.getActivity(this@buildNotification, requestCode, intent, pendingIntentFlags))
        setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
    }
}

@SuppressLint("MissingPermission")
fun NotificationManagerCompat.notifyCompat(context: Context, notificationId: Int, build: Notification) {
    if (SDK_INT < 33 || context.hasPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS))) {
        notify(notificationId, build)
    }
}

@SuppressLint("MissingPermission")
fun NotificationManagerCompat.notifyCompat(
    tag: String,
    notificationId: Int,
    notification: Notification
) {
    if (SDK_INT < 33 || appCtx.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
        notify(tag, notificationId, notification)
    }
}

fun Context.cancelNotification(notificationId: Int) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(notificationId)
}
