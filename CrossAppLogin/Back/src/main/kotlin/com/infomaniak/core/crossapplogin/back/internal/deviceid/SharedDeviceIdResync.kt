/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
@file:OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class, ExperimentalSplittiesApi::class)

package com.infomaniak.core.crossapplogin.back.internal.deviceid

import android.os.Messenger
import android.os.SystemClock
import com.infomaniak.core.common.Xor
import com.infomaniak.core.common.trySending
import com.infomaniak.core.common.updateOnce
import com.infomaniak.core.crossapplogin.back.putBundleWrappedDataInObj
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import splitties.init.appCtx
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class SharedDeviceIdResync(
    private val targetPackageNames: suspend () -> Set<String>,
) {

    private val localResyncingMutableFlow = MutableStateFlow<ResyncRequest?>(null)
    private val localResyncingFlow: StateFlow<ResyncRequest?> = localResyncingMutableFlow.asStateFlow()
    private val priorExternalResync = Channel<ResyncRequest>()
    private val externalResyncMutex = Mutex()

    suspend fun tryResyncSharedDeviceId(ourSharedDeviceId: Uuid): Boolean {
        try {
            localResyncingMutableFlow.updateOnce(valueToWaitFor = null, newValue = ResyncRequest.now(ourSharedDeviceId))
            val resultSuccessStatusOrNewIdFromExternalResync: Xor<Boolean, Uuid> = raceOf(
                { Xor.First(resyncSharedDeviceId(ourSharedDeviceId)) },
                { Xor.Second(priorExternalResync.receive().id) }
            )
            return when (resultSuccessStatusOrNewIdFromExternalResync) {
                is Xor.First -> resultSuccessStatusOrNewIdFromExternalResync.value
                is Xor.Second -> {
                    // We don't do this inside the race to ensure we don't update the device id while resync is running
                    // and not canceled yet.
                    SharedDeviceIdStorage.setDeviceId(resultSuccessStatusOrNewIdFromExternalResync.value)
                    false
                }
            }
        } finally {
            localResyncingMutableFlow.value = null
        }
    }

    suspend fun handleSharedDeviceIdResyncRequest(
        trustedClientMessenger: Messenger,
        externalRequest: ResyncRequest
    ) {
        externalResyncMutex.withLock {
            val counterRequest = onExternalResyncIncoming(externalRequest)
            val response: ResyncResponse = if (counterRequest != null) {
                ResyncResponse.AlreadySyncing(counterRequest)
            } else {
                val updated = SharedDeviceIdStorage.updateDeviceId(externalRequest)
                if (updated) ResyncResponse.Updated else ResyncResponse.AlreadySynced
            }
            val responseBytes = ProtoBuf.encodeToByteArray(response)
            val succeeded = trustedClientMessenger.trySending { newMessage ->
                newMessage.putBundleWrappedDataInObj(responseBytes)
            }
            if (succeeded) updateSyncStatus(
                packageName = externalRequest.sourceAppPackageName,
                id = externalRequest.id,
                status = SyncStatus.SyncedExternally
            )
        }
    }

    private fun onExternalResyncIncoming(request: ResyncRequest): ResyncRequest? {
        val localResyncing = localResyncingFlow.value
        if (localResyncing != null) {
            if (localResyncing.startedBefore(request)) return localResyncing
            else priorExternalResync.trySend(request)
        }
        return null
    }

    private suspend fun resyncSharedDeviceId(ourSharedDeviceId: Uuid): Boolean {
        val packages = targetPackageNames()
        SharedDeviceIdStorage.cleanSyncStatusesOfUninstalledApps(packages)

        var shouldRetry = false

        val appsToSendSyncReportTo = packages.filter { packageName ->
            when (getSyncStatusForApp(packageName, ourSharedDeviceId)) {
                SyncStatus.Synced -> return@filter true
                SyncStatus.SyncedExternally, SyncStatus.SyncedAndReportSaved -> return@filter false
                null -> Unit
            }
            when (val response = requestResync(packageName, ourSharedDeviceId)) {
                ResyncResponse.AlreadySynced, ResyncResponse.Updated -> {
                    updateSyncStatus(packageName, ourSharedDeviceId, SyncStatus.Synced)
                    return@filter true
                }
                is ResyncResponse.AlreadySyncing -> {
                    SharedDeviceIdStorage.setDeviceId(response.priorRequest.id)
                    return false
                }
                null -> {
                    shouldRetry = true
                    return@filter false
                }
            }
        }.reversed() // We go backwards so processes are more likely to still be alive.

        if (appsToSendSyncReportTo.isEmpty()) return !shouldRetry

        val resyncReport = ResyncReport(
            id = ourSharedDeviceId,
            syncedApps = getSyncedPackages(ourSharedDeviceId)
        )
        appsToSendSyncReportTo.forEach { packageName ->
            val succeeded = sendResyncReport(
                packageName = packageName,
                report = resyncReport,
            )
            when {
                succeeded -> updateSyncStatus(packageName, ourSharedDeviceId, SyncStatus.SyncedAndReportSaved)
                else -> shouldRetry = true
            }
        }
        return !shouldRetry
    }

    private suspend fun requestResync(packageName: String, ourSharedDeviceId: Uuid): ResyncResponse? {
        TODO("Send our cross-app-device-id over RESYNC_SHARED_DEVICE_ID_REQUEST")
        TODO("Get result from RESYNC_SHARED_DEVICE_ID_RESPONSE")

    }

    private suspend fun getSyncStatusForApp(packageName: String, expectedSharedDeviceId: Uuid): SyncStatus? {
        return SharedDeviceIdStorage.getSyncStatusForApp(packageName, expectedSharedDeviceId)
    }

    private suspend fun updateSyncStatus(packageName: String, id: Uuid, status: SyncStatus) {
        SharedDeviceIdStorage.updateSyncStatus(packageName, id, status)
    }

    private suspend fun getSyncedPackages(expectedSharedDeviceId: Uuid): Set<String> {
        return SharedDeviceIdStorage.getPackagesWithStatus(expectedSharedDeviceId, SyncStatus.Synced)
    }

    private suspend fun sendResyncReport(packageName: String, report: ResyncReport): Boolean {
        TODO("Send our cross-app-device-id over RESYNC_SHARED_DEVICE_ID_REPORT")
        TODO("Get result from RESYNC_SHARED_DEVICE_ID_REPORT_SAVED")
    }

    @Serializable
    data class ResyncRequest(
        @ProtoNumber(1) val sourceAppPackageName: String,
        @ProtoNumber(2) val id: Uuid,
        @ProtoNumber(3) val elapsedRealtimeNanos: Long,
    ) {

        fun startedBefore(other: ResyncRequest): Boolean = elapsedRealtimeNanos < other.elapsedRealtimeNanos

        companion object {
            fun now(id: Uuid) = ResyncRequest(
                sourceAppPackageName = appCtx.packageName,
                id = id,
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            )
        }
    }

    @Serializable
    @SerialName("\u0000")
    sealed interface ResyncResponse {

        @Serializable
        @SerialName("\u0001")
        data object AlreadySynced : ResyncResponse

        @Serializable
        @SerialName("\u0002")
        data object Updated : ResyncResponse

        @Serializable
        @SerialName("\u0003")
        data class AlreadySyncing(@ProtoNumber(1) val priorRequest: ResyncRequest) : ResyncResponse
    }

    @Serializable
    data class ResyncReport(
        @ProtoNumber(1) val id: Uuid,
        @ProtoNumber(2) val syncedApps: Set<String>,
    )

    @Serializable
    enum class SyncStatus {
        @ProtoNumber(1) Synced,
        @ProtoNumber(2) SyncedAndReportSaved,
        @ProtoNumber(3) SyncedExternally,
    }
}
