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
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.cancellable
import com.infomaniak.core.completableScope
import com.infomaniak.core.sentry.SentryLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import splitties.init.appCtx
import java.io.FileNotFoundException

object NotificationsRegistrationManager {

    internal const val FCM_TOKEN_RETRIEVAL_FAILED_MESSAGE = "Couldn't retrieve the FCM token!"

    private const val TAG = "NotificationsRegistrationManager"

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
        Dispatchers.IO {
            lastSyncedKeyFile.writeText(text)
        }
    }

    suspend inline fun <reified T : AbstractNotificationsRegistrationWorker> scheduleWorkerOnUpdate(
        noinline latestNotificationTopics: (userId: Int) -> Flow<List<String>>,
    ): Nothing = scheduleWorkerOnUpdate(latestNotificationTopics, T::class.java)

    @PublishedApi
    internal suspend fun <T : AbstractNotificationsRegistrationWorker> scheduleWorkerOnUpdate(
        latestNotificationTopics: (userId: Int) -> Flow<List<String>>,
        workerClass: Class<T>
    ): Nothing = Dispatchers.Default {

        val latestFcmToken = fcmTokenUpdates.filterNotNull().onStart {
            val token = runCatching {
                Firebase.messaging.token.await()
            }.cancellable().getOrElse { t ->
                // If there are "normal" cases where this happens, decrease the priority of this Sentry log.
                return@onStart SentryLog.e(TAG, FCM_TOKEN_RETRIEVAL_FAILED_MESSAGE, t)
            }
            emit(token)
        }.stateIn(this)

        val userIdsFlow = UserDatabase().userDao().allUsers.map { users -> users.map { it.id } }.distinctUntilChanged()

        latestFcmToken.collectLatest { fcmToken ->
            userIdsFlow.collectLatest { userIds ->
                while (true) {
                    awaitAnyNeedForUpdate(userIds, fcmToken, latestNotificationTopics)
                    val id = AbstractNotificationsRegistrationWorker.schedule(workerClass)
                    AbstractNotificationsRegistrationWorker.awaitWorkerCompletion(id) // Don't loop immediately.
                }
            }
        }
        awaitCancellation() // Should never be reached since latestFcmToken is a StateFlow.
    }

    private suspend fun awaitAnyNeedForUpdate(
        userIds: List<Int>,
        fcmToken: String,
        latestNotificationTopics: (userId: Int) -> Flow<List<String>>
    ) {
        completableScope { completable ->
            for (userId in userIds) launch {
                awaitNeedForUpdate(
                    fcmToken = fcmToken,
                    userId = userId,
                    latestNotificationTopics = latestNotificationTopics(userId)
                )
                completable.complete(Unit)
            }
        }
    }

    private suspend fun awaitNeedForUpdate(
        fcmToken: String,
        userId: Int,
        latestNotificationTopics: Flow<List<String>>
    ) {
        latestNotificationTopics.first { topics ->
            needsUpdate(
                registrationInfo = RegistrationInfo.create(fcmToken, topics),
                userId = userId.toLong()
            )
        }
    }

    private fun lastSyncKeyFileForUser(userId: Long): AtomicFile = AtomicFile(lastSyncedKeyDir.resolve("$userId"))
}
