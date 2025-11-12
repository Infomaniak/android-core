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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.infomaniak.core

import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import splitties.init.appCtx

private val compatNotificationManager by lazy { NotificationManagerCompat.from(appCtx) }

/**
 * Detect when notifications are enabled, and if they are currently disabled.
 *
 * Why such a weird wording? Well, as of API 36, Android kills the app process when
 * notifications are disabled (just like for any permission), so the flow can't actually emit
 * false if the initial value was true.
 *
 * @see isChannelEnabledFlow
 * @see areChannelsEnabledFlow
 */
@Suppress("UnusedReceiverParameter") // Receiver for discoverability
@RequiresApi(28)
fun NotificationManager.areNotificationsEnabledFlow(): Flow<Boolean> = broadcastReceiverFlow(
    action = NotificationManager.ACTION_APP_BLOCK_STATE_CHANGED,
    emitInitialEmptyIntent = true
).conflate().map { intent ->
    if (intent.hasExtra(NotificationManager.EXTRA_BLOCKED_STATE)) {
        intent.getBooleanExtra(NotificationManager.EXTRA_BLOCKED_STATE, false).not()
    } else { // Post registration empty Intent, get the current state.
        compatNotificationManager.areNotificationsEnabled()
    }
}

/**
 * Emits true if the given notification channel is enabled, has its group enabled (if any),
 * while the app has notifications enabled too.
 *
 * NOTE: If the channel doesn't exist when this flow is first collected, it will emit null,
 * and will not retry. Only changes will make it through. Consequently, **make sure to start
 * collecting this flow AFTER creating the notification channel groups and channels**.
 *
 * In short, this function expects the channel to have been already created, and to not be deleted either.
 *
 * Both of these things are in control of the code in the app.
 *
 * @see areChannelsEnabledFlow
 * @see isChannelEnabled
 */
fun NotificationManager.isChannelEnabledFlow(channelId: String): Flow<Boolean?> = channelFlow {
    if (SDK_INT >= 28) {
        val channelGroupId = getNotificationChannel(channelId)?.group
        combine(
            areNotificationsEnabledFlow(),
            isChannelEnabledReceiverFlow(channelId),
            if (channelGroupId == null) flowOf(true) else isChannelGroupEnabledReceiverFlow(channelGroupId),
        ) { appNotificationsEnabled, channelEnabled, channelGroupEnabled ->
            when {
                appNotificationsEnabled.not() -> false
                channelGroupEnabled == false -> false
                channelGroupEnabled == null -> false // Deleted channel group
                else -> channelEnabled
            }
        }.collect { send(it) }
    } else {
        ProcessLifecycleOwner.get().lifecycle.isResumedFlow().collect { isResumed ->
            if (isResumed) {
                send(isChannelEnabled(channelId))
            }
        }
    }
}.conflate().flowOn(Dispatchers.IO).distinctUntilChanged()

/**
 * Emits a map containing a `Boolean?` for each passed id passed in [channelIds].
 *
 * This Boolean is true if the channel is enabled, all while its group (if any), and the app notifications are enabled too.
 * If the channel is not found (i.e. not created, or deleted by the app), the value will be `null`.
 *
 * @see isChannelEnabledFlow
 * @see isChannelEnabled
 */
@RequiresApi(28)
fun NotificationManager.areChannelsEnabledFlow(
    channelIds: List<String>
): Flow<Map<String, Boolean?>> = broadcastReceiverFlow(
    filter = IntentFilter().also {
        it.addAction(NotificationManager.ACTION_APP_BLOCK_STATE_CHANGED)
        it.addAction(NotificationManager.ACTION_NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED)
        it.addAction(NotificationManager.ACTION_NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED)
    },
    emitInitialEmptyIntent = true
).conflate().map { _ ->
    // We expect the channels enabled state to not move frequently,
    // so instead of keeping our cache, and looking into the Intent data to update it,
    // we query the current state each time.
    // That keeps the code simple, and it's only slightly less efficient
    // when the user changes notification settings for the host app.
    channelIds.associateWith { isChannelEnabled(it) }
}.flowOn(Dispatchers.IO).conflate().distinctUntilChanged()

/**
 * Checks if the given channel is currently enabled.
 * Also checks its group is enabled (unless [checkGroup] is set to false), and if the specified group is not found,
 * considers it deleted, and considers this child channel to be disabled.
 *
 * @see isChannelEnabledFlow
 * @see areChannelsEnabledFlow
 */
fun NotificationManager.isChannelEnabled(
    channelId: String,
    checkApp: Boolean = true,
    checkGroup: Boolean = true,
): Boolean? {
    val channel = getNotificationChannel(channelId) ?: return null
    if (channel.importance == NotificationManager.IMPORTANCE_NONE) return false
    if (checkApp && compatNotificationManager.areNotificationsEnabled().not()) return false
    if (checkGroup) channel.group?.let { groupId ->
        if (compatNotificationManager.getNotificationChannelGroupCompat(groupId)?.isBlocked ?: true) return false
    }
    return true
}

/**
 * ONLY gives the channel state. Its group, if any, and notifications for the entire app can still be disabled.
 *
 * emits `null` if the channel didn't exist when first collected.
 */
@RequiresApi(28)
private fun NotificationManager.isChannelEnabledReceiverFlow(channelId: String): Flow<Boolean?> = broadcastReceiverFlow(
    action = NotificationManager.ACTION_NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED,
    emitInitialEmptyIntent = true
).conflate().transform { intent ->
    when (intent.getStringExtra(NotificationManager.EXTRA_NOTIFICATION_CHANNEL_ID)) {
        null -> { // Post registration empty Intent, get the current state.
            emit(isChannelEnabled(channelId, checkApp = false, checkGroup = false))
        }
        channelId -> emit(intent.getBooleanExtra(NotificationManager.EXTRA_BLOCKED_STATE, false).not())
        else -> Unit
    }
}.distinctUntilChanged()

/**
 * ONLY gives the channel enabled state. Notifications for the entire app can still be disabled.
 *
 * emits `null` if the channel group didn't exist when first collected.
 */
@RequiresApi(28)
private fun NotificationManager.isChannelGroupEnabledReceiverFlow(groupId: String): Flow<Boolean?> = broadcastReceiverFlow(
    action = NotificationManager.ACTION_NOTIFICATION_CHANNEL_GROUP_BLOCK_STATE_CHANGED,
    emitInitialEmptyIntent = true
).conflate().transform { intent ->
    when (intent.getStringExtra(NotificationManager.EXTRA_NOTIFICATION_CHANNEL_GROUP_ID)) {
        null -> { // Post registration empty Intent, get the current state.
            emit(getNotificationChannelGroup(groupId)?.isBlocked?.not())
        }
        groupId -> emit(intent.getBooleanExtra(NotificationManager.EXTRA_BLOCKED_STATE, false).not())
        else -> Unit
    }
}.distinctUntilChanged()
