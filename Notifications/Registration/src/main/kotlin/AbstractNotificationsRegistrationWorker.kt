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
import com.infomaniak.core.cancellable
import com.infomaniak.core.network.INFOMANIAK_API_V1
import com.infomaniak.core.sentry.SentryLog
import createHttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import splitties.init.appCtx
import java.io.IOException
import java.util.UUID

abstract class AbstractNotificationsRegistrationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    protected abstract suspend fun getConnectedHttpClient(userId: Int): OkHttpClient

    protected abstract suspend fun currentTopicsForUser(userId: Int): List<String>

    final override suspend fun doWork(): Result {
        SentryLog.i(TAG, "Work started")
        val fcmToken = runCatching {
            Firebase.messaging.token.await()
        }.cancellable().getOrElse { t ->
            // If there are "normal" cases where this happens, decrease the priority of this Sentry log.
            SentryLog.wtf(TAG, NotificationsRegistrationManager.FCM_TOKEN_RETRIEVAL_FAILED_MESSAGE, t)
            return Result.retry()
        }
        val currentUsers = UserDatabase().userDao().allUsers.first()
        val outcomes = coroutineScope {
            currentUsers.map { user ->
                async {
                    registerNotificationsForUser(userId = user.id, fcmToken = fcmToken)
                }
            }
        }.awaitAll()
        return if (outcomes.any { it == Outcome.ShouldRetry }) {
            Result.retry()
        } else {
            SentryLog.i(TAG, "Work finished")
            Result.success()
        }
    }

    private suspend fun registerNotificationsForUser(userId: Int, fcmToken: String): Outcome {
        val registrationInfo = RegistrationInfo.create(
            token = fcmToken,
            topics = currentTopicsForUser(userId)
        )

        val needsUpdate = NotificationsRegistrationManager.needsUpdate(registrationInfo, userId.toLong())
        if (needsUpdate.not()) return Outcome.Done

        val okHttpClient = getConnectedHttpClient(userId = userId)
        val httpClient = createHttpClient(okHttpClient)
        return runCatching {
            val response = httpClient.post("$INFOMANIAK_API_V1/devices/register") {
                setBody(registrationInfo)
            }
            if (response.status.isSuccess()) {
                SentryLog.i(TAG, "doWork: $userId has been registered")
                NotificationsRegistrationManager.updateLastSyncedData(registrationInfo, userId = userId.toLong())
                SentryLog.i(TAG, "doWork: $userId has updateLastSyncedData done")
                Outcome.Done
            } else {
                handleUnsuccessfulResponse(response)
            }
        }.cancellable().getOrElse { t ->
            handleNetworkOperationFailure(t)
        }
    }

    companion object {
        suspend inline fun <T : AbstractNotificationsRegistrationWorker> schedule(workerClass: Class<T>): UUID {
            val workManager = WorkManager.getInstance(appCtx)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(workerClass)
                .setConstraints(constraints)
                .build()
            SentryLog.i("NotificationsRegistrationWorker", "before enqueueUniqueWork")
            workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            ).await()
            SentryLog.i("NotificationsRegistrationWorker", "after enqueueUniqueWork")
            return workRequest.id
        }

        suspend fun awaitWorkerCompletion(uuid: UUID) {
            val workManager = WorkManager.getInstance(appCtx)
            workManager.getWorkInfoByIdFlow(uuid).first { it?.state?.isFinished == true }
        }

        @PublishedApi
        internal const val UNIQUE_WORK_NAME = "notifications-registration"
    }
}

private enum class Outcome {
    Done,
    ShouldRetry,
}

private const val TAG = "NotificationsRegistrationWorker"

private fun handleUnsuccessfulResponse(response: HttpResponse): Outcome {
    val httpStatusCode = response.status.value
    val errorMessage = "registerNotificationsForUser led to http $httpStatusCode"
    return when (httpStatusCode) {
        in 500..<600 -> {
            SentryLog.e(TAG, errorMessage)
            Outcome.ShouldRetry
        }
        401 -> {
            SentryLog.w(TAG, errorMessage)
            Outcome.Done
        }
        else -> {
            SentryLog.wtf(TAG, errorMessage)
            Outcome.Done
        }
    }
}

private fun handleNetworkOperationFailure(t: Throwable): Outcome {
    val errorMessage = "registerNotificationsForUser failed: ${t.javaClass}"
    return if (t is IOException) {
        SentryLog.i(TAG, errorMessage, t)
        Outcome.ShouldRetry
    } else {
        SentryLog.wtf(TAG, errorMessage, t)
        Outcome.Done
    }
}
