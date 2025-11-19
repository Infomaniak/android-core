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

package com.infomaniak.core.crossapplogin.back

import android.content.Context
import android.content.Intent
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.lifecycle.Lifecycle
import com.infomaniak.core.DynamicLazyMap
import com.infomaniak.core.android.service.OnBindingIssue
import com.infomaniak.core.android.service.OnServiceDisconnectionBehavior
import com.infomaniak.core.android.service.withBoundService
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.cancellable
import com.infomaniak.core.crossapplogin.back.internal.ChannelMessageHandler
import com.infomaniak.core.crossapplogin.back.internal.DisposableMessage
import com.infomaniak.core.crossapplogin.back.internal.certificates.AppCertificateChecker
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdManager
import com.infomaniak.core.flowForKey
import com.infomaniak.core.sentry.SentryLog
import com.infomaniak.core.sharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import splitties.coroutines.raceOf
import splitties.experimental.ExperimentalSplittiesApi
import splitties.init.appCtx
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class CrossAppLoginImpl(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : CrossAppLogin() {

    private val certificateChecker = AppCertificateChecker.withInfomaniakApps
    private val ourPackageName = context.packageName

    private val ipcIssuesManager: IpcIssuesManager = IpcIssuesManagerImpl

    @ExperimentalSerializationApi
    override fun accountsFromOtherApps(hostLifecycle: Lifecycle): Flow<List<ExternalAccount>> = channelFlow {

        val isStartedFlow = hostLifecycle.currentStateFlow.map { it.isAtLeast(Lifecycle.State.STARTED) }.stateIn(this)

        val accountsPerApp = DynamicLazyMap.sharedFlow(
            cacheManager = { _, _ -> awaitCancellation() },
            coroutineScope = this,
            createFlow = { packageName: String ->
                isStartedFlow.transform { isStarted -> if (isStarted) emit(retrieveAccountsFromApp(packageName)) }
            }
        )

        val targetPackageNames = isStartedFlow.transform { isStarted -> if (isStarted) emit(targetPackageNames()) }.stateIn(this)
        val alreadyConnectedEmailsFlow: StateFlow<Set<String>> = UserDatabase().userDao().allUsers.map { users ->
            users.mapTo(mutableSetOf()) { it.email }
        }.stateIn(this)

        val accountsFlow = MutableStateFlow(emptyList<ExternalAccount>())

        targetPackageNames.collectLatest { packages ->
            packages.forEach { targetPackage ->
                launch {
                    alreadyConnectedEmailsFlow.collect { alreadyConnectedEmails ->
                        send(accountsFlow.updateAndGet { it.withAccounts(emptyList(), alreadyConnectedEmails) })
                    }
                }
                launch {
                    accountsPerApp.flowForKey(targetPackage).collect { accounts ->
                        send(accountsFlow.updateAndGet { it.withAccounts(accounts, alreadyConnectedEmailsFlow.value) })
                    }
                }
            }
        }
    }.flowOn(Dispatchers.Default).conflate()

    private fun List<ExternalAccount>.withAccounts(
        accounts: List<ExternalAccount>,
        alreadyConnectedEmails: Set<String>
    ): List<ExternalAccount> {
        return (this + accounts)
            .filter { it.email !in alreadyConnectedEmails }
            .groupBy { it.email }
            .map { (_, externalAccounts) ->
                val account = externalAccounts.firstOrNull { it.isCurrentlySelectedInAnApp } ?: externalAccounts.first()
                account.copy(tokens = externalAccounts.flatMapTo(mutableSetOf()) { it.tokens })
            }
            .sortedBy { it.isCurrentlySelectedInAnApp } // false comes before true, so selected accounts will be last in the list.
    }

    @ExperimentalUuidApi
    override val sharedDeviceIdFlow: SharedFlow<Uuid> get() = sharedDeviceIdManager.crossAppDeviceIdFlow

    @ExperimentalUuidApi
    override val sharedDeviceIdManager: SharedDeviceIdManager = SharedDeviceIdManager(
        context = context,
        coroutineScope = coroutineScope,
        ipcIssuesManager = ipcIssuesManager,
        certificateChecker = certificateChecker,
        targetPackageNames = ::targetPackageNames
    )

    private suspend fun targetPackageNames(): Set<String> {
        val resolveInfoList = Dispatchers.IO { appCtx.packageManager.queryIntentServices(Intent(INTENT_ACTION), 0) }
        return resolveInfoList.mapNotNullTo(mutableSetOf()) { info ->
            info.serviceInfo.packageName.takeUnless { it == ourPackageName }
        }
    }

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromApp(packageName: String): List<ExternalAccount> = raceOf(
        {
            val isTrustedApp = certificateChecker.isPackageNameAllowed(packageName) == true
            if (isTrustedApp) awaitCancellation()
            emptyList() // Return early if the app isn't trusted, or not installed.
        },
        {
            // Start connecting to the app immediately, during the signing certificate check.
            val externalAccounts = retrieveAccountsFromUncheckedApp(packageName)
            // Check we can trust the result, or drop it.
            val isTrustedApp = certificateChecker.isPackageNameAllowed(packageName) == true
            if (isTrustedApp) externalAccounts else emptyList()
        }
    )

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromUncheckedApp(packageName: String): List<ExternalAccount> {
        val intent = Intent().also {
            it.`package` = packageName
            it.action = INTENT_ACTION
        }
        return runCatching {
            retrieveAccountsFromUncheckedService(intent)
        }.cancellable().getOrElse { t ->
            SentryLog.e(TAG, "Couldn't retrieve accounts in app $packageName", t)
            emptyList()
        }
    }

    @ExperimentalSerializationApi
    private suspend fun retrieveAccountsFromUncheckedService(service: Intent): List<ExternalAccount> {
        val targetPackageName = service.`package`!!
        val bytesOrNull = context.withBoundService(
            service = service,
            timeoutConnection = ipcIssuesManager.timeoutConnection(targetPackageName, isUserWaiting = true),
            onDisconnected = { OnServiceDisconnectionBehavior.UnbindImmediately(null) },
            onBindingIssue = {
                ipcIssuesManager.logBindingIssue(targetPackageName, it)
                OnBindingIssue.GiveUp(null)
            },
            flags = Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT,
            block = { binder ->
                val messenger = Messenger(binder)
                val replies = Channel<DisposableMessage>(capacity = 1)
                val replyHandler = ChannelMessageHandler(replies)
                val replyTo = Messenger(replyHandler)

                try {
                    val request = Message.obtain().also {
                        it.what = BaseCrossAppLoginService.IpcMessageWhat.GET_SNAPSHOT_OF_SIGNED_IN_ACCOUNTS
                        it.replyTo = replyTo
                    }
                    messenger.send(request)
                    raceOf(
                        { replies.receive().use { response -> response.unwrapByteArrayOrNull() } },
                        {
                            ipcIssuesManager.timeoutOperation(
                                operationName = "retrieveAccountsFromUncheckedService",
                                targetPackageName = targetPackageName,
                                acceptableDuration = 10.seconds, // This long in case I/O gets very slow on low-end devices.
                            )
                            null
                        }
                    )
                } catch (_: RemoteException) {
                    null
                }
            },
        )

        return bytesOrNull?.let { bytes -> ProtoBuf.decodeFromByteArray<List<ExternalAccount>>(bytes) } ?: emptyList()
    }

    private companion object {
        const val TAG = "CrossAppLoginImpl"

        private const val INTENT_ACTION = "com.infomaniak.crossapp.login"
    }
}
