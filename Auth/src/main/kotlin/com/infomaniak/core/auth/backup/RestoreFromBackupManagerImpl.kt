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
@file:OptIn(ExperimentalSplittiesApi::class, ExperimentalSerializationApi::class)

package com.infomaniak.core.auth.backup

import androidx.core.util.AtomicFile
import com.infomaniak.core.auth.AuthConfiguration.clientId
import com.infomaniak.core.auth.DerivedTokenGenerator
import com.infomaniak.core.auth.DerivedTokenGeneratorImpl
import com.infomaniak.core.auth.api.ApiRoutesCore.TOKEN_URL
import com.infomaniak.core.auth.models.user.User
import com.infomaniak.core.auth.room.UserDatabase
import com.infomaniak.core.common.extensions.write
import com.infomaniak.core.common.getAndroidId
import com.infomaniak.core.network.networking.HttpUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.invoke
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import splitties.experimental.ExperimentalSplittiesApi
import splitties.init.appCtx
import java.io.FileNotFoundException

internal class RestoreFromBackupManagerImpl(
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : RestoreFromBackupManager() {

    private val restorationStateFile = AtomicFile(appCtx.filesDir.resolve("accountsRestorationState"))

    private val derivedTokenGenerator: DerivedTokenGenerator = DerivedTokenGeneratorImpl(
        coroutineScope = coroutineScope,
        tokenRetrievalUrl = TOKEN_URL,
        clientId = clientId,
        userAgent = HttpUtils.getUserAgent,
    )

    override val state: SharedFlow<State> = flow {
        try {
            val lastSavedRestorationState = readLastSavedRestorationState()
            performRestorationHandlingIfNeeded(lastSavedRestorationState)
        } catch (_: FileNotFoundException) {
            saveNoRestorationInProgress()
        }
        emit(State.Settled)
    }.shareIn(coroutineScope, SharingStarted.Eagerly)

    override suspend fun ensureRestorationIsHandled() {
        state.first { it is State.Settled }
    }

    private suspend fun FlowCollector<State>.performRestorationHandlingIfNeeded(lastState: AccountsRestorationState) {
        val users = UserDatabase.instance.userDao().allUsers()
        val currentAndroidId = getAndroidId()
        val deviceChanged = currentAndroidId != lastState.androidId
        restoreAccounts(
            currentAndroidId = currentAndroidId,
            alreadyRestoredAccountIds = if (deviceChanged) emptySet() else lastState.restoredAccountIds,
            allUsers = users
        )
    }

    private suspend fun FlowCollector<State>.restoreAccounts(
        currentAndroidId: String,
        alreadyRestoredAccountIds: Set<Long>,
        allUsers: List<User>,
    ) {
        val allUsersIds = allUsers.mapTo(hashSetOf()) { it.id.toLong() }
        if (alreadyRestoredAccountIds.containsAll(allUsersIds)) return
        emit(State.RestoringFromBackup)
        saveRestorationState(
            AccountsRestorationState(
                androidId = currentAndroidId,
                restoredAccountIds = alreadyRestoredAccountIds
            )
        )
        TODO("Loop trying to derive tokens for remaining accounts. Allow retrying or giving-up if needed on each iteration")
    }

    private suspend fun saveNoRestorationInProgress() {
        val users = UserDatabase.instance.userDao().allUsers()
        // To avoid running a token derivation, we mark it as done if we
        // didn't have it before.
        val data = AccountsRestorationState(
            androidId = getAndroidId(),
            restoredAccountIds = users.mapTo(hashSetOf()) { it.id.toLong() }
        )
        saveRestorationState(data)
    }

    private suspend fun readLastSavedRestorationState(): AccountsRestorationState = Dispatchers.IO {
        restorationStateFile.openRead().use { stream ->
            ProtoBuf.decodeFromByteArray<AccountsRestorationState>(stream.readBytes())
        }
    }

    private suspend fun saveRestorationState(currentState: AccountsRestorationState) = Dispatchers.IO {
        restorationStateFile.write { outputStream ->
            outputStream.write(ProtoBuf.encodeToByteArray(currentState))
        }
    }
}
