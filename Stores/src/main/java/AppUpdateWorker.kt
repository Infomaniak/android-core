/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.lib.stores

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import com.infomaniak.lib.core.utils.SentryLog
import com.infomaniak.lib.stores.updatemanagers.WorkerUpdateManager
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AppUpdateWorker(appContext: Context, params: WorkerParameters) : ListenableWorker(appContext, params) {

    private val updateManager = WorkerUpdateManager(appContext)

    override fun startWork(): ListenableFuture<Result> {
        SentryLog.i(TAG, "Work started")

        return CallbackToFutureAdapter.getFuture { completer ->
            updateManager.installDownloadedUpdate(
                onInstallSuccess = { completer.setResult(Result.success()) },
                onInstallFailure = { exception ->
                    // This avoid the user being instantly reprompted to download update
                    Sentry.captureException(exception)
                    completer.setResult(Result.failure())
                },
            )
        }
    }

    private fun CallbackToFutureAdapter.Completer<Result>.setResult(result: Result) {
        set(result)
        SentryLog.d(TAG, "Work finished")
    }

    class Scheduler(appContext: Context, private val workManager: WorkManager = WorkManager.getInstance(appContext)) {

        private val storesLocalSettings = StoresLocalSettings.getInstance(appContext)

        fun scheduleWorkIfNeeded() {
            if (BuildConfig.HAS_GPLAY_SERVICES && storesLocalSettings.hasAppUpdateDownloaded) {
                SentryLog.d(TAG, "Work scheduled")

                val workRequest = OneTimeWorkRequestBuilder<AppUpdateWorker>()
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    // We start with a delayed duration, so that when the app quickly come back to foreground because the user
                    // was just switching apps, the service is not launched
                    .setInitialDelay(INITIAL_DELAY_SECONDS, TimeUnit.SECONDS)
                    .build()

                workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, workRequest)
            }
        }

        suspend fun cancelWorkIfNeeded() = withContext(Dispatchers.IO) {

            val workInfos = workManager.getWorkInfos(
                WorkQuery.Builder.fromUniqueWorkNames(listOf(TAG))
                    .addStates(listOf(WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED))
                    .build(),
            ).get()

            workInfos.forEachIndexed { index, workInfo ->
                workManager.cancelWorkById(workInfo.id)
                SentryLog.d(TAG, "Work cancelled")
                // TODO: Check this Sentry in approximately 1 month (end of March 2024) to know if the `forEach` is useful or not.
                if (index > 0) {
                    Sentry.withScope { scope ->
                        scope.level = SentryLevel.WARNING
                        Sentry.captureMessage("There is more than one work infos, we must keep forEach")
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "AppUpdateWorker"
        private const val INITIAL_DELAY_SECONDS = 10L
    }
}
