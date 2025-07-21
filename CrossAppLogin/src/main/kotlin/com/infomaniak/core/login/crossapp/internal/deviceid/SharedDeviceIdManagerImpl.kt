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
import com.infomaniak.core.login.crossapp.IpcIssuesManager
import com.infomaniak.core.login.crossapp.internal.ChannelMessageHandler
import com.infomaniak.core.login.crossapp.internal.DisposableMessage
import com.infomaniak.core.login.crossapp.internal.certificates.AppCertificateChecker
import com.infomaniak.core.login.crossapp.putBundleWrappedDataInObj
import com.infomaniak.core.login.crossapp.unwrapByteArrayOrNull
import com.infomaniak.lib.core.utils.SentryLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.invoke
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
internal class SharedDeviceIdManagerImpl(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val ipcIssuesManager: IpcIssuesManager,
    private val certificateChecker: AppCertificateChecker,
    private val targetPackageNames: Set<String>
) : SharedDeviceIdManager() {

    private val sharedDeviceIdMutex = SharedDeviceIdManager.sharedDeviceIdMutex
    private val storage = SharedDeviceIdManager.storage

    override val crossAppDeviceIdFlow: SharedFlow<Uuid> = storage.writtenIdFlow.onStart {
        emit(getCrossAppDeviceId())
    }.distinctUntilChanged().shareIn(
        scope = coroutineScope,
        started = SharingStarted.Lazily,
        replay = 1
    )

    override suspend fun findCrossAppDeviceId(request: CrossAppDeviceIdRequest): Uuid? {
        storage.readDeviceId()?.let { return it }
        sharedDeviceIdMutex.withLock {
            storage.readDeviceId()?.let { return it }
            return null //TODO: Remove this line once we have 3+ apps to test through-app cross-app device id retrieval.
            val id = findCrossAppDeviceIdInApps(request) ?: return null
            storage.setDeviceId(id)
            return id
        }
    }

    private suspend fun getCrossAppDeviceId(): Uuid {
        storage.readDeviceId()?.let { return it }
        sharedDeviceIdMutex.withLock {
            storage.readDeviceId()?.let { return it }
            val id = retrieveAndSyncDeviceId()
            storage.setDeviceId(id)
            return id
        }
    }

    private suspend fun findCrossAppDeviceIdInApps(request: CrossAppDeviceIdRequest): Uuid? = completableScope { completable ->

        val packagesToLookup = targetPackageNames - request.packageNamesAlreadyBeingChecked

        val packageNamesToSkip = buildSet<String> {
            add(context.packageName)
            addAll(request.packageNamesAlreadyBeingChecked)
            addAll(targetPackageNames)
        }

        packagesToLookup.forEach { packageName ->
            launch {
                if (certificateChecker.isPackageNameAllowed(packageName) != true) return@launch
                val id = attemptGettingDeviceId(packageName, packageNamesToSkip) ?: return@launch
                completable.complete(id)
            }
        }
        null
    }

    private suspend fun retrieveAndSyncDeviceId(): Uuid = coroutineScope {
        val deviceIdCompletable = CompletableDeferred<Uuid>()
        val allPackagesCheckedJob = Job(parent = coroutineContext.job)
        val syncJobs = targetPackageNames.map { packageName ->
            val thisPackageCheckedJob = Job(parent = allPackagesCheckedJob)
            async {
                if (certificateChecker.isPackageNameAllowed(packageName) == true) {
                    attemptGettingOrSyncingDeviceId(
                        deviceIdCompletable = deviceIdCompletable,
                        targetPackageName = packageName,
                        thisPackageCheckedJob = thisPackageCheckedJob
                    )
                } else {
                    thisPackageCheckedJob.complete() // Avoid blocking its parent job if the packageName wasn't allowed.
                    null
                }
            }
        }
        // Technically, we could have the sync part happen concurrently, and return the value ASAP, if available,
        // but we're doing this optimization yet since there's only a single other app that we'll be reaching when launching
        // this feature.
        allPackagesCheckedJob.complete() // Will actually complete once all child jobs created above complete.
        allPackagesCheckedJob.join() // All packages were checked...
        if (deviceIdCompletable.isCompleted.not()) { // It's now safe to check if deviceId could be retrieved.
            @OptIn(ExperimentalUuidApi::class)
            val id = Dispatchers.IO { Uuid.random() }
            deviceIdCompletable.complete(id) // Send the newly generated id, so apps we're connected to can receive it.
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
        deviceIdCompletable: CompletableDeferred<Uuid>,
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
                timeoutConnection = ipcIssuesManager.timeoutConnection(targetPackageName),
                onDisconnected = { OnServiceDisconnectionBehavior.UnbindImmediately(null) },
                onBindingIssue = {
                    ipcIssuesManager.logBindingIssue(targetPackageName, it)
                    OnBindingIssue.GiveUp(null)
                },
                flags = Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT,
                block = { binder ->
                    raceOf({
                        getOrSyncDeviceId(binder, deviceIdCompletable, thisPackageCheckedJob)
                    }, {
                        ipcIssuesManager.timeoutOperation(operationName = "getOrSyncDeviceId", targetPackageName)
                        null
                    })
                }
            )
        } finally {
            thisPackageCheckedJob.complete() // Ensure it's complete. Needed for service binding issues.
        }
    }

    private suspend fun getOrSyncDeviceId(
        binder: IBinder,
        deviceIdCompletable: CompletableDeferred<Uuid>,
        thisPackageCheckedJob: CompletableJob,
    ): Result<Unit> = runCatching {
        // requestSharedDeviceId isn't supposed to throw, so this wrapping `runCatching` shouldn't be necessary,
        // but if a dev mistake ever happens, and breaks the communication format, it's still best to not crash.
        requestSharedDeviceId(binder, packageNamesToSkip = targetPackageNames + context.packageName)
    }.also { // This is the `runCatching` equivalent to a `finally` block for a `try` block.
        thisPackageCheckedJob.complete()
    }.cancellable().onSuccess { id ->
        if (id != null) deviceIdCompletable.complete(id)
    }.mapCatching { id ->
        if (id == null) {
            val id = deviceIdCompletable.await()
            sendSharedDeviceId(binder, id) // Sync it to the target app.
        }
    }

    private suspend fun attemptGettingDeviceId(
        targetPackageName: String,
        packageNamesToSkip: Set<String>,
    ): Uuid? = context.withBoundService<Uuid?>(
        service = Intent().also {
            it.`package` = targetPackageName
            it.action = BaseCrossAppLoginService.ACTION
        },
        timeoutConnection = ipcIssuesManager.timeoutConnection(targetPackageName),
        onDisconnected = { OnServiceDisconnectionBehavior.UnbindImmediately(null) },
        onBindingIssue = {
            ipcIssuesManager.logBindingIssue(targetPackageName, it)
            OnBindingIssue.GiveUp(null)
        },
        flags = Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT,
        block = { binder ->
            runCatching {
                // requestSharedDeviceId isn't supposed to throw, so this wrapping `runCatching` shouldn't be necessary,
                // but if a dev mistake ever happens, and breaks the communication format, it's still best to not crash.
                raceOf(
                    { requestSharedDeviceId(binder, packageNamesToSkip = packageNamesToSkip) },
                    {
                        ipcIssuesManager.timeoutOperation(operationName = "requestSharedDeviceId", targetPackageName)
                        null
                    }
                )
            }.cancellable().getOrNull()
        }
    )

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun requestSharedDeviceId(binder: IBinder, packageNamesToSkip: Set<String>): Uuid? {
        val messenger = Messenger(binder)
        val replies = Channel<DisposableMessage>(capacity = 1)
        val replyHandler = ChannelMessageHandler(replies)
        val replyTo = Messenger(replyHandler)
        return try {
            val request = Message.obtain().also {
                it.what = BaseCrossAppLoginService.IpcMessageWhat.GET_SHARED_DEVICE_ID
                it.replyTo = replyTo
                val request = CrossAppDeviceIdRequest(packageNamesAlreadyBeingChecked = packageNamesToSkip)
                it.putBundleWrappedDataInObj(ProtoBuf.encodeToByteArray(request))
            }
            Dispatchers.IO { messenger.send(request) }
            replies.receive().use { response -> response.unwrapByteArrayOrNull()?.let { Uuid.fromByteArray(it) } }
        } catch (_: RemoteException) {
            null
        }
    }

    private suspend fun sendSharedDeviceId(binder: IBinder, newId: Uuid) {
        val messenger = Messenger(binder)
        try {
            val request = Message.obtain().also {
                it.what = BaseCrossAppLoginService.IpcMessageWhat.SYNC_SHARED_DEVICE_ID
                it.putBundleWrappedDataInObj(newId.toByteArray())
            }
            Dispatchers.IO { messenger.send(request) }
        } catch (_: RemoteException) {
        }
    }

    private companion object {
        const val TAG = "SharedDeviceIdManagerImpl"
    }
}
