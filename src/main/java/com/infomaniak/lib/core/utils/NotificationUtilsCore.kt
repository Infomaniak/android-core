/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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
package com.infomaniak.lib.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.infomaniak.lib.core.R

abstract class NotificationUtilsCore {

    fun Context.buildNotification(
        channelId: String,
        icon: Int,
        title: String,
        description: String? = null
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channelId).apply {
            setTicker(title)
            setAutoCancel(true)
            setContentTitle(title)
            description?.let { setStyle(NotificationCompat.BigTextStyle().bigText(it)) }
            setSmallIcon(icon)
        }
    }

    fun Context.cancelNotification(notificationId: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Context.createNotificationChannels(
        channelList: List<NotificationChannel>,
        groupList: List<NotificationChannelGroup>? = null
    ) {
        (getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager).apply {
            groupList?.let { createNotificationChannelGroups(it) }
            createNotificationChannels(channelList)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Context.deleteNotificationChannels(channelList: List<String>) {
        (getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager).apply {
            channelList.forEach { deleteNotificationChannel(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    protected fun Context.buildNotificationChannel(
        channelId: String,
        name: String,
        importance: Int,
        description: String? = null,
        groupId: String? = null
    ): NotificationChannel {
        return NotificationChannel(channelId, name, importance).apply {
            description?.let { this.description = it }
            groupId?.let { group = it }
            when (importance) {
                NotificationManager.IMPORTANCE_HIGH -> {
                    enableLights(true)
                    setShowBadge(true)
                    lightColor = getColor(R.color.primary)
                }
                else -> {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(false)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun NotificationManagerCompat.notifyCompat(context: Context, notificationId: Int, build: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.hasPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS))) {
                notify(notificationId, build)
            }
        } else {
            notify(notificationId, build)
        }
    }

    companion object {
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }
}
