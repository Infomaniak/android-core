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
package com.infomaniak.core.login.crossapp.internal.deviceinfo

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
import com.infomaniak.core.autoCancelScope
import com.infomaniak.core.cancellable
import com.infomaniak.core.login.crossapp.CrossAppLogin
import com.infomaniak.lib.core.api.ApiRoutesCore
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.networking.HttpUtils
import com.infomaniak.lib.core.networking.ManualAuthorizationRequired
import com.infomaniak.lib.core.room.UserDatabase
import com.infomaniak.lib.core.utils.SentryLog
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import splitties.init.appCtx
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class AbstractDeviceInfoUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        suspend inline fun <reified T : AbstractDeviceInfoUpdateWorker> schedule() {
            val workManager = WorkManager.getInstance(appCtx)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<T>()
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 10L, TimeUnit.MINUTES)
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

        val currentUsers = UserDatabase().userDao().allUsers.first()
        if (currentUsers.isEmpty()) return@autoCancelScope Result.success()

        val deviceInfoUpdateManager = DeviceInfoUpdateManager.sharedInstance
        val sharedDeviceIdManager = CrossAppLogin.Companion.forContext(
            context = applicationContext,
            coroutineScope = this
        ).sharedDeviceIdManager

        val currentCrossAppDeviceId = sharedDeviceIdManager.crossAppDeviceIdFlow.first()

        val deviceInfo = currentDeviceInfo(currentCrossAppDeviceId)
        val outcomes = currentUsers.map { user ->
            async {
                attemptUpdatingDeviceInfoIfNeeded(
                    deviceInfoUpdateManager = deviceInfoUpdateManager,
                    targetUser = user,
                    deviceInfo = deviceInfo,
                    crossAppDeviceId = currentCrossAppDeviceId
                )
            }
        }.awaitAll()
        when {
            outcomes.any { it == Outcome.ShouldRetry } -> Result.retry()
            else -> Result.success()
        }
    }

    private suspend fun attemptUpdatingDeviceInfoIfNeeded(
        deviceInfoUpdateManager: DeviceInfoUpdateManager,
        targetUser: User,
        deviceInfo: DeviceInfo,
        crossAppDeviceId: Uuid
    ): Outcome = runCatching {
        if (deviceInfoUpdateManager.isUpToDate(crossAppDeviceId, targetUser.id.toLong())) {
            return Outcome.Done
        }
        val okHttpClient = getConnectedHttpClient(userId = targetUser.id)
        val httpClient = HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            install(ContentNegotiation) {
                json()
            }
        }

        SentryLog.i(TAG, "Will attempt updating device info for user id ${targetUser.id}")

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
            deviceInfoUpdateManager.updateLastSyncedKey(crossAppDeviceId, targetUser.id.toLong())
            SentryLog.i(TAG, "attemptUpdatingDeviceInfoIfNeeded succeeded")
            Outcome.Done
        } else {
            val httpStatusCode = response.status.value
            val errorMessage = "attemptUpdatingDeviceInfoIfNeeded led to http $httpStatusCode"
            if (httpStatusCode in 500..599) {
                SentryLog.i(TAG, errorMessage)
                Outcome.ShouldRetry
            } else {
                SentryLog.wtf(TAG, errorMessage)
                Outcome.Done
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
                isComputer -> DeviceInfo.Type.Computer
                isFoldable -> DeviceInfo.Type.Phone
                hasTabletSizedScreen -> DeviceInfo.Type.Tablet
                else -> DeviceInfo.Type.Phone
            },
            uuidV4 = currentCrossAppDeviceId.toHexDashString()
        )
    }
}
