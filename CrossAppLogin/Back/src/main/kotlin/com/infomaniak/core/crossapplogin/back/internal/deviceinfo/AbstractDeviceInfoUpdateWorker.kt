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

package com.infomaniak.core.crossapplogin.back.internal.deviceinfo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import com.infomaniak.core.DynamicLazyMap
import com.infomaniak.core.auth.api.ApiRoutesCore
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.autoCancelScope
import com.infomaniak.core.cancellable
import com.infomaniak.core.crossapplogin.back.CrossAppLogin
import com.infomaniak.core.crossapplogin.back.internal.deviceinfo.DeviceInfo.Type
import com.infomaniak.core.network.networking.HttpUtils
import com.infomaniak.core.network.networking.ManualAuthorizationRequired
import com.infomaniak.core.sentry.SentryLog
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import okhttp3.OkHttpClient
import splitties.init.appCtx
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class AbstractDeviceInfoUpdateWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    companion object {

        suspend inline fun <reified T : AbstractDeviceInfoUpdateWorker> schedule() {
            val workManager = WorkManager.getInstance(appCtx)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<T>()
                .setBackoffCriteria(BackoffPolicy.LINEAR, backoffDelay = 10L, timeUnit = TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            workManager.enqueueUniqueWork(
                "device-info-update",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            ).await()
        }

        private const val TAG = "AbstractDeviceInfoUpdateWorker"
    }

    protected abstract suspend fun getConnectedHttpClient(userId: Int): OkHttpClient

    override suspend fun doWork(): Result = autoCancelScope {

        val currentUsersFlow = UserDatabase().userDao().allUsers.stateIn(this)
        if (currentUsersFlow.value.isEmpty()) return@autoCancelScope Result.success()

        val deviceInfoUpdateManager = DeviceInfoUpdateManager.sharedInstance
        val sharedDeviceIdManager = CrossAppLogin.Companion.forContext(
            context = applicationContext,
            coroutineScope = this
        ).sharedDeviceIdManager

        val currentCrossAppDeviceId = sharedDeviceIdManager.crossAppDeviceIdFlow.first()

        val deviceInfo = currentDeviceInfo(currentCrossAppDeviceId)

        val deviceInfoUpdatersForUserId = DynamicLazyMap<Long, Deferred<Outcome>>(
            cacheManager = { userId, deferred ->
                if (deferred.isCompleted && deferred.getCompleted() == Outcome.Done) awaitCancellation()
                delay(10.milliseconds) // Don't drop too soon, so mapLatest triggering doesn't lead to dropping the work.
            },
            coroutineScope = this,
            createElement = { userId ->
                async {
                    attemptUpdatingDeviceInfoIfNeeded(
                        deviceInfoUpdateManager = deviceInfoUpdateManager,
                        targetUserId = userId,
                        deviceInfo = deviceInfo,
                        crossAppDeviceId = currentCrossAppDeviceId
                    )
                }
            }
        )

        val outcomes = currentUsersFlow
            .map { users -> users.mapTo(mutableSetOf()) { it.id.toLong() } }
            .distinctUntilChanged()
            .mapLatest { userIds ->
                deviceInfoUpdatersForUserId.useElements(userIds) {
                    it.values.awaitAll()
                }
            }.first()
        when {
            outcomes.any { it == Outcome.ShouldRetry } -> Result.retry()
            else -> Result.success()
        }
    }

    private suspend fun attemptUpdatingDeviceInfoIfNeeded(
        deviceInfoUpdateManager: DeviceInfoUpdateManager,
        targetUserId: Long,
        deviceInfo: DeviceInfo,
        crossAppDeviceId: Uuid,
    ): Outcome = runCatching {
        if (deviceInfoUpdateManager.isUpToDate(crossAppDeviceId, targetUserId)) {
            return Outcome.Done
        }
        val okHttpClient = getConnectedHttpClient(userId = targetUserId.toInt())
        val httpClient = HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            install(ContentNegotiation) {
                json()
            }
        }

        SentryLog.i(TAG, "Will attempt updating device info for user id $targetUserId")

        val url = ApiRoutesCore.sendDeviceInfo()
        val response = httpClient.post(url) {
            headers {
                @OptIn(ManualAuthorizationRequired::class) // Already handled by the http client.
                HttpUtils.getHeaders().forEach { (header, value) -> append(header, value) }
            }
            contentType(ContentType.Application.Json)
            setBody(deviceInfo)
        }
        if (response.status.isSuccess()) {
            deviceInfoUpdateManager.updateLastSyncedKey(crossAppDeviceId, targetUserId)
            SentryLog.i(TAG, "attemptUpdatingDeviceInfoIfNeeded succeeded")
            Outcome.Done
        } else {
            val httpStatusCode = response.status.value
            val errorMessage = "attemptUpdatingDeviceInfoIfNeeded led to http $httpStatusCode"
            when (httpStatusCode) {
                in 500..599 -> {
                    SentryLog.i(TAG, errorMessage)
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
    }.cancellable().getOrElse {
        it.printStackTrace()
        SentryLog.i(TAG, "attemptUpdatingDeviceInfoIfNeeded failed: ${it.javaClass}")
        if (it is IOException) Outcome.ShouldRetry else Outcome.Done
    }

    private enum class Outcome {
        Done,
        ShouldRetry,
    }

    @ExperimentalUuidApi
    private fun currentDeviceInfo(currentCrossAppDeviceId: Uuid): DeviceInfo {
        val hasTabletSizedScreen = appCtx.resources.configuration.smallestScreenWidthDp >= 600
        val packageManager = appCtx.packageManager
        val isFoldable = when {
            SDK_INT >= 30 -> packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)
            else -> false
        }
        val isComputer = packageManager.hasSystemFeature(PackageManager.FEATURE_PC)
        return DeviceInfo(
            brand = Build.BRAND,
            model = Build.MODEL,
            platform = "android",
            type = when {
                isComputer -> Type.Computer
                isFoldable -> Type.Phone
                hasTabletSizedScreen -> Type.Tablet
                else -> Type.Phone
            },
            uuidV4 = currentCrossAppDeviceId.toHexDashString(),
        )
    }
}
