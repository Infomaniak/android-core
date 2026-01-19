/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
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
package com.infomaniak.core.crossapplogin.back

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.infomaniak.core.crossapplogin.back.internal.deviceid.SharedDeviceIdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class CrossAppLogin {

    companion object {
        fun forContext(
            context: Context,
            coroutineScope: CoroutineScope
        ): CrossAppLogin = CrossAppLoginImpl(context, coroutineScope)
    }

    /**
     * Each time the passed [hostLifecycle] transitions to [Lifecycle.State.STARTED],
     * emits a merge of accounts found in other known apps, as they arrive.
     *
     * Must return selected accounts **last** (account(s) currently selected in other apps).
     */
    @ExperimentalSerializationApi
    internal abstract fun accountsFromOtherApps(hostLifecycle: Lifecycle): Flow<AccountsFromOtherApps>

    internal data class AccountsFromOtherApps(
        val accounts: List<ExternalAccount>,
        val waitingForMoreApps: Boolean,
    ) {
        companion object {
            fun none() = AccountsFromOtherApps(emptyList(), waitingForMoreApps = false)
        }
    }

    /**
     * Gives an id for this device, that is shared across all our apps.
     *
     * This might involve generating said id.
     *
     * If the id is ever updated, it will be emitted in this shared flow.
     */
    @ExperimentalUuidApi
    abstract val sharedDeviceIdFlow: SharedFlow<Uuid>

    @ExperimentalUuidApi
    internal abstract val sharedDeviceIdManager: SharedDeviceIdManager
}
