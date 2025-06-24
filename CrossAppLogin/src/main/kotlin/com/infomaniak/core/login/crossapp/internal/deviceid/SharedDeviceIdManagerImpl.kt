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
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import com.infomaniak.core.android.service.OnBindingIssue
import com.infomaniak.core.android.service.OnServiceDisconnectionBehavior
import com.infomaniak.core.android.service.withBoundService
import com.infomaniak.core.cancellable
import com.infomaniak.core.completableScope
import com.infomaniak.core.login.crossapp.BaseCrossAppLoginService
import com.infomaniak.core.login.crossapp.CrossAppDeviceIdRequest
import com.infomaniak.core.login.crossapp.internal.ChannelMessageHandler
import com.infomaniak.core.login.crossapp.internal.DisposableMessage
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateChecker
import com.infomaniak.core.login.crossapp.putBundleWrappedDataInObj
import com.infomaniak.core.login.crossapp.putBundleWrappedStringInObj
import com.infomaniak.core.login.crossapp.unwrapStringOrNull
import com.infomaniak.lib.core.utils.SentryLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.invoke
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class SharedDeviceIdManagerImpl(
    private val context: Context,
    private val certificateChecker: AppCertificateChecker,
    private val targetPackageNames: Set<String>
) : SharedDeviceIdManager {

    private val sharedDeviceIdMutex = SharedDeviceIdManager.sharedDeviceIdMutex
    private val storage = SharedDeviceIdManager.storage

    override suspend fun getCrossAppDeviceId(): String {
        storage.readDeviceId()?.let { return it }
        sharedDeviceIdMutex.withLock {
            storage.readDeviceId()?.let { return it }
            val id = retrieveAndSyncDeviceId()
            storage.setDeviceId(id)
            return id
        }
    }

    override suspend fun findCrossAppDeviceId(packageNamesToSkip: Set<String>): String? {
        storage.readDeviceId()?.let { return it }
        sharedDeviceIdMutex.withLock {
            storage.readDeviceId()?.let { return it }
            val id = findDeviceId(
                targetPackageNames = targetPackageNames - packageNamesToSkip - context.packageName
            ) ?: return null
            storage.setDeviceId(id)
            return id
        }
    }

    private suspend fun findDeviceId(targetPackageNames: Set<String>): String? = completableScope { completable ->
        targetPackageNames.forEach { packageName ->
            launch {
                if (certificateChecker.isPackageNameAllowed(packageName) != true) return@launch
                val id = attemptGettingDeviceId(packageName) ?: return@launch
                completable.complete(id)
            }
        }
        null
    }

    private suspend fun retrieveAndSyncDeviceId(): String = coroutineScope {
        val deviceIdCompletable = CompletableDeferred<String>()
        val allPackagesCheckedJob = Job(parent = coroutineContext.job)
        val syncJobs = targetPackageNames.map { packageName ->
            val thisPackageCheckedJob = Job(parent = allPackagesCheckedJob)
            async {
                if (certificateChecker.isPackageNameAllowed(packageName) == true) attemptGettingOrSyncingDeviceId(
                    deviceIdCompletable = deviceIdCompletable,
                    targetPackageName = packageName,
                    thisPackageCheckedJob = thisPackageCheckedJob
                ) else null
            }
        }
        // Technically, we could have the sync part happen concurrently, and return the value ASAP, if available,
        // but we're doing this optimization yet since there's only a single other app that we'll be reaching when launching
        // this feature.
        allPackagesCheckedJob.complete() // Will actually complete once all child jobs created above complete.
        allPackagesCheckedJob.join()
        if (deviceIdCompletable.isCompleted.not()) {
            @OptIn(ExperimentalUuidApi::class)
            val id = Dispatchers.IO { Uuid.random().toHexDashString() }
            deviceIdCompletable.complete(id)
        }
        syncJobs.awaitAll().forEach { result ->
            result?.onFailure {
                SentryLog.e(TAG, "Issue while attempting to retrieve or sync the cross-app device id", it)
            }
        }
        deviceIdCompletable.await()
    }

    /**
     * For technical reasons, the device id is not returned as a result, but handed over to [deviceIdCompletable].
     *
     * This allows retrieval from other concurrent checks if there's a need to sync it.
     */
    private suspend fun attemptGettingOrSyncingDeviceId(
        deviceIdCompletable: CompletableDeferred<String>,
        targetPackageName: String,
        thisPackageCheckedJob: CompletableJob,
    ): Result<Unit>? {
        try {
            val intent = Intent().also {
                it.`package` = targetPackageName
                it.action = BaseCrossAppLoginService.ACTION
            }
            return context.withBoundService<Result<Unit>?>(
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
                            sendSharedDeviceId(binder, id) // Sync it to the target app.
                        }
                    }
                }
            )
        } finally {
            thisPackageCheckedJob.complete()
        }
    }

    private suspend fun attemptGettingDeviceId(
        targetPackageName: String,
    ): String? = context.withBoundService<String?>(
        service = Intent().also {
            it.`package` = targetPackageName
            it.action = BaseCrossAppLoginService.ACTION
        },
        timeoutConnection = { isWaitingForReconnect -> delay(10.seconds) },
        onDisconnected = { OnServiceDisconnectionBehavior.UnbindImmediately(null) },
        onBindingIssue = { OnBindingIssue.GiveUp(null) },
        flags = Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT,
        block = { binder ->
            requestSharedDeviceId(binder)
        }
    )

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

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun requestSharedDeviceId(binder: IBinder): String? {
        val messenger = Messenger(binder)
        val replies = Channel<DisposableMessage>(capacity = 1)
        val replyHandler = ChannelMessageHandler(replies)
        val replyTo = Messenger(replyHandler)
        return try {
            val request = Message.obtain().also {
                it.what = BaseCrossAppLoginService.IpcMessageWhat.GET_SHARED_DEVICE_ID
                it.replyTo = replyTo
                val request = CrossAppDeviceIdRequest(packageNamesAlreadyBeingChecked = targetPackageNames.toSet())
                it.putBundleWrappedDataInObj(ProtoBuf.encodeToByteArray(request))
            }
            Dispatchers.IO { messenger.send(request) }
            replies.receive().use { response -> response.unwrapStringOrNull() }
        } catch (_: RemoteException) {
            null
        }
    }

    private suspend fun sendSharedDeviceId(binder: IBinder, newId: String) {
        val messenger = Messenger(binder)
        try {
            val request = Message.obtain().also {
                it.what = BaseCrossAppLoginService.IpcMessageWhat.SYNC_SHARED_DEVICE_ID
                it.putBundleWrappedStringInObj(newId)
            }
            Dispatchers.IO { messenger.send(request) }
        } catch (_: RemoteException) {
        }
    }

    private companion object {
        const val TAG = "SharedDeviceIdManagerImpl"
    }
}
