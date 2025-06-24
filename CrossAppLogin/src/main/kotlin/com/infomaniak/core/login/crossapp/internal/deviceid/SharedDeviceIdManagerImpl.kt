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
@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core.login.crossapp.internal.deviceid

import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.infomaniak.core.android.service.OnBindingIssue
import com.infomaniak.core.android.service.OnServiceDisconnectionBehavior
import com.infomaniak.core.android.service.withBoundService
import com.infomaniak.core.cancellable
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateChecker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.invoke
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.withLock
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class SharedDeviceIdManagerImpl(
    private val context: Context,
    private val certificateChecker: AppCertificateChecker,
    private val targetPackageNames: List<String>
) : SharedDeviceIdManager {

    private val sharedDeviceIdMutex = SharedDeviceIdManager.sharedDeviceIdMutex
    private val storage = SharedDeviceIdManager.storage

    override suspend fun getSharedDeviceId(): String {
        //  1. If we already have an id, return it, otherwise, continue.
        // 2. Acquire `sharedDeviceIdLock`
        // 3. If we now have an id, return it, otherwise, continue.
        storage.readDeviceId()?.let { return it }
        sharedDeviceIdMutex.withLock {
            storage.readDeviceId()?.let { return it }
            //TODO:
            // 4. Generate id for proposal.
            // 5. Contact all known apps with the "START_SHARED_DEVICE_ID_SYNC" message, giving them the list of apps we're reaching //REMOVE THIS?, and the id proposal
            //   a. If we already have an id, return it, otherwise, continue.
            //   b. Acquire our `sharedDeviceIdLock`
            //   c. If we now have an id, return it (releasing the sharedDeviceLock), otherwise, continue.
            //   d. If there are other other apps that we know but didn't appear in the given list: ask them and return the result combination.
            //   e. Otherwise return that we don't have the id, and wait (with timeout for the finish event with the id to persist).
            // 6. Wait until one app gives us an id, or until all apps reply they don't have it
            // 7. Persist our sharedDeviceId to the returned one (if a contacted app gave us one), or to the one we initially generated.
            // 8. Release `sharedDeviceIdLock`
            // 9. Send "FINISH_SHARED_DEVICE_ID_SYNC" to all apps that didn't have the shared device id already, with the id and the entire list of checked apps.
            //   a. Persist the given shared device id.
            //   b. Release `sharedDeviceIdLock`
            //   c. Forward the message to all apps we know that weren't checked and didn't have the shared id yet.

            // TODO: handle non updated apps (ignore them).
            //TODO: Handle apps being updated, by retrying if we detect the lastUpdateTime changed, or if we find a packageInstaller update session.
            TODO()
        }
    }

    private suspend fun retrieveAndSyncDeviceId(): String = coroutineScope {
        val deviceIdCompletable = CompletableDeferred<String>()
        val allPackagesCheckedJob = Job(parent = coroutineContext.job)
        val syncJobs = targetPackageNames.map { packageName ->
            val intent = Intent().also {
                it.`package` = packageName
                it.action = "com.infomaniak.crossapp.login"
            }
            val thisPackageCheckedJob = Job(parent = allPackagesCheckedJob)
            async {
                context.withBoundService<Result<Unit>?>(
                    service = intent,
                    timeoutConnection = { isWaitingForReconnect -> delay(10.seconds) },
                    onDisconnected = { OnServiceDisconnectionBehavior.UnbindImmediately(null) },
                    onBindingIssue = { OnBindingIssue.GiveUp(null) },
                    flags = Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT,
                    block = { binder ->
                        val result = try {
                            attemptGettingDeviceId(
                                binder = binder,
                                deviceIdCompletable = deviceIdCompletable,
                            )
                        } finally {
                            thisPackageCheckedJob.complete()
                        }
                        result.mapCatching { hadTheId ->
                            if (hadTheId.not()) {
                                val id = deviceIdCompletable.await()
                                sendSharedDeviceId(binder, id)
                            }
                        }
                    }
                )
            }
        }
        allPackagesCheckedJob.complete() // Will actually complete once all child jobs complete.
        raceOf(
            { deviceIdCompletable.await() },
            {
                allPackagesCheckedJob.join()
                @OptIn(ExperimentalUuidApi::class)
                val id = Dispatchers.IO { Uuid.random().toHexDashString() }
                deviceIdCompletable.complete(id)
                TODO()
            },
            {
                syncJobs.awaitAll()
                TODO("")
            },
        ).also {
            allPackagesCheckedJob.cancel()
        }
        // Technically, we could have the sync part happen concurrently, and return the value ASAP, if available,
        // but we're doing this optimization yet since there's only a single other app that we'll be reaching when launching
        // this feature.
    }

    private suspend fun attemptGettingDeviceId(
        binder: IBinder,
        deviceIdCompletable: CompletableDeferred<String>
    ): Result<Boolean> = runCatching {
        val id = requestSharedDeviceId(binder)
        if (id != null) {
            deviceIdCompletable.complete(id)
            true
        } else {
            false
        }
    }.cancellable()

//            val id = Dispatchers.IO { Uuid.random().toHexDashString() }

    private suspend fun requestSharedDeviceId(binder: IBinder): String? {
        TODO()
    }

    private suspend fun sendSharedDeviceId(binder: IBinder, newId: String) {
        TODO()
    }
}
