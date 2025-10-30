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
package com.infomaniak.core.notifications.registration

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.network.INFOMANIAK_API_V1
import com.infomaniak.core.network.api.ApiController
import com.infomaniak.core.network.models.ApiResponse
import com.infomaniak.core.network.models.exceptions.NetworkException
import com.infomaniak.core.sentry.SentryLog
import io.sentry.Sentry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.OkHttpClient
import splitties.init.appCtx
import java.util.concurrent.TimeUnit

abstract class AbstractNotificationsRegistrationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    protected abstract suspend fun getConnectedHttpClient(userId: Int): OkHttpClient

    protected abstract suspend fun currentTopicsForUser(): List<String>

    final override suspend fun doWork(): Result {
        SentryLog.i(TAG, "Work started")
        val fcmToken = Firebase.messaging.token.await()
        val currentUsers = UserDatabase().userDao().allUsers.first()
        val outcomes = supervisorScope { // If one throws, don't cancel the others.
            currentUsers.map { user ->
                async {
                    registerNotificationsForUser(
                        userId = user.id,
                        fcmToken = fcmToken
                    )
                }
            }
        }.awaitAll()
        return if (outcomes.any { it == Outcome.ShouldRetry }) Result.retry() else Result.success()
    }

    private suspend fun registerNotificationsForUser(userId: Int, fcmToken: String): Outcome {
        val okHttpClient = getConnectedHttpClient(userId = userId)
        val registrationInfo = RegistrationInfo.create(
            token = fcmToken,
            topics = currentTopicsForUser()

        )
        val apiResponse: ApiResponse<Boolean> = ApiController.callApi(
            url = "$INFOMANIAK_API_V1/devices/register",
            method = ApiController.ApiMethod.POST,
            body = ApiController.json.encodeToJsonElement(registrationInfo),
            okHttpClient = okHttpClient,
            useKotlinxSerialization = true
        )
        return if (apiResponse.isSuccess()) {
            SentryLog.i(TAG, "doWork: $userId has been registered")
            NotificationsRegistrationManager.sharedInstance.updateLastSyncedData(registrationInfo, userId = userId.toLong())
            SentryLog.i(TAG, "doWork: $userId has updateLastSyncedData done")
            Outcome.Done
        } else {
            runCatching {
                apiResponse.throwErrorAsException()
                TODO("Replace with usage of ktor")
                Outcome.Done
            }.getOrElse { exception ->
                if (exception !is NetworkException) Sentry.captureException(exception)
                SentryLog.w(TAG, "launchWork: register $userId failed", exception)
                Outcome.ShouldRetry
            }
        }
    }

    private enum class Outcome {
        Done,
        ShouldRetry,
    }

    companion object {
        suspend inline fun <T : AbstractNotificationsRegistrationWorker> schedule(workerClass: Class<T>) {
            val workManager = WorkManager.getInstance(appCtx)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(workerClass)
                .setBackoffCriteria(BackoffPolicy.LINEAR, backoffDelay = 10L, timeUnit = TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            workManager.enqueueUniqueWork(
                "notifications-registration",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            ).await()
        }
        private const val TAG = "NotificationsRegistrationWorker"
    }
}

inline fun <reified T> ApiResponse<T>.throwErrorAsException(): Nothing = throw getApiException()

inline fun <reified T> ApiResponse<T>.getApiException(): Exception {
    return error?.exception ?: ApiErrorException(ApiController.json.encodeToString(this))
}
