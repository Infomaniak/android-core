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
package com.infomaniak.core.twofactorauth.back

import java.io.IOException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
interface TwoFactorAuth {

    /**
     * Retrieves the latest login challenge, if any.
     *
     * Returns null otherwise, or in case of another issue (network or backend related).
     *
     * Might retry several times before returning.
     */
    suspend fun tryGettingLatestChallenge(): RemoteChallenge?

    suspend fun approveChallenge(challengeUid: Uuid): Outcome

    suspend fun rejectChallenge(challengeUid: Uuid): Outcome

    sealed interface Outcome {
        data object Success : Outcome
        sealed interface Issue : Outcome {
            data object Expired : Issue
            data class ErrorResponse(val httpStatusCode: Int) : Issue
            data class Network(val exception: IOException) : Issue
            data class Unknown(val exception: Throwable) : Issue
        }
    }
}
