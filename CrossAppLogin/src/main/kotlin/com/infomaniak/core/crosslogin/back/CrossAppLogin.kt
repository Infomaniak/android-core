/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.infomaniak.core.crosslogin.back

import android.content.Context
import com.infomaniak.core.crosslogin.back.internal.deviceid.SharedDeviceIdManager
import kotlinx.coroutines.CoroutineScope
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
     * Returns a merge of accounts found in other known apps.
     *
     * Must return selected accounts **last** (account(s) currently selected in other apps).
     */
    @ExperimentalSerializationApi
    abstract suspend fun retrieveAccountsFromOtherApps(): List<ExternalAccount>

    /**
     * Gives an id for this device, that is shared across all our apps.
     *
     * This might involve generating said id.
     *
     * If the id is ever updated, it will be emitted in this shared flow.
     */
    @ExperimentalUuidApi
    @PublishedApi
    internal abstract val sharedDeviceIdFlow: SharedFlow<Uuid>

    @ExperimentalUuidApi
    internal abstract val sharedDeviceIdManager: SharedDeviceIdManager
}
