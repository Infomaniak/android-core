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

package com.infomaniak.core.notifications.registration

import android.util.AtomicFile
import androidx.core.util.readText
import androidx.core.util.writeText
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.ktx.messaging
import com.infomaniak.core.anyConcurrent
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.cancellable
import com.infomaniak.core.sentry.SentryLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.invoke
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import splitties.init.appCtx
import java.io.FileNotFoundException

class NotificationsRegistrationManager private constructor() {

    companion object {
        val sharedInstance = NotificationsRegistrationManager()

        @PublishedApi
        internal const val TAG = "NotificationsRegistrationManager"
    }

    private val lastSyncedKeyDir = appCtx.filesDir.resolve("lastSyncedNotificationRegistrationInfo")

    private val fcmTokenUpdates = MutableSharedFlow<String>(replay = 1)

    /**
     * To be called from [FirebaseMessagingService.onNewToken]
     */
    fun onNewToken(fcmToken: String) {
        fcmTokenUpdates.tryEmit(fcmToken)
    }

    suspend fun resetForUser(userId: Long) = Dispatchers.IO {
        lastSyncKeyFileForUser(userId).delete()
    }

    suspend fun needsUpdate(registrationInfo: RegistrationInfo, userId: Long): Boolean = Dispatchers.IO {
        try {
            val lastSyncedKeyFile = lastSyncKeyFileForUser(userId)
            val lastSyncedData = lastSyncedKeyFile.readText()
            val latestData = Json.encodeToString(registrationInfo)
            lastSyncedData != latestData
        } catch (_: FileNotFoundException) {
            true
        }
    }

    suspend fun updateLastSyncedData(registrationInfo: RegistrationInfo, userId: Long) {
        val lastSyncedKeyFile = lastSyncKeyFileForUser(userId)
        val text = Json.encodeToString(registrationInfo)
        lastSyncedKeyFile.writeText(text)
    }

    suspend inline fun <reified T : AbstractNotificationsRegistrationWorker> scheduleWorkerOnUpdate(
        latestNotificationTopics: Flow<List<String>>,
    ): Nothing = scheduleWorkerOnUpdate(latestNotificationTopics, T::class.java)

    @PublishedApi
    internal suspend fun <T : AbstractNotificationsRegistrationWorker> scheduleWorkerOnUpdate(
        latestNotificationTopics: Flow<List<String>>,
        workerClass: Class<T>
    ): Nothing = Dispatchers.Default {

        val latestRegistrationInfo = fcmTokenUpdates.filterNotNull().onStart {
            val token = runCatching {
                Firebase.messaging.token.await()
            }.cancellable().onFailure { t ->
                SentryLog.e(TAG, "Couldn't get the Firebase Cloud Messaging token", t)
            }.getOrNull() ?: return@onStart
            emit(token)
        }.flatMapLatest { token ->
            latestNotificationTopics.map { RegistrationInfo.create(token = token, it) }
        }.stateIn(this)

        val userIdsFlow = UserDatabase().userDao().allUsers.map { users -> users.map { it.id } }.distinctUntilChanged()

        latestRegistrationInfo.collectLatest { registrationInfo ->
            userIdsFlow.collect { userIds ->
                val needsUpdate = userIds.anyConcurrent { needsUpdate(registrationInfo, it.toLong()) }
                if (needsUpdate) AbstractNotificationsRegistrationWorker.schedule(workerClass)
            }
        }
        awaitCancellation()
    }

    private fun lastSyncKeyFileForUser(userId: Long): AtomicFile = AtomicFile(lastSyncedKeyDir.resolve("$userId"))
}
