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

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class TwoFactorAuthTestImpl(
    override val userId: Int,
) : TwoFactorAuth {

    /**
     * Retrieves the latest login challenge, if any.
     *
     * Returns null otherwise, or in case of another issue (network or backend related).
     *
     * Might retry several times before returning.
     */
    override suspend fun tryGettingLatestChallenge(): RemoteChallenge? {
        delay(2.seconds)
        return RemoteChallenge(
            uuid = Uuid.random(),
            device = RemoteChallenge.Device(
                name = "Pixel 9 Pro Fold",
                type = RemoteChallenge.Device.Type.Phone
            ),
            type = RemoteChallenge.Type.Approval,
            location = "Geneva, Switzerland",
            createdAt = System.currentTimeMillis() / 1000L - 58L,
            expiresAt = System.currentTimeMillis() / 1000L + 300L
        )
    }

    override suspend fun approveChallenge(challengeUid: Uuid): TwoFactorAuth.Outcome {
        delay(1.5.seconds)
        return TwoFactorAuth.Outcome.Success
    }

    override suspend fun rejectChallenge(challengeUid: Uuid): TwoFactorAuth.Outcome {
        delay(1.5.seconds)
        return TwoFactorAuth.Outcome.Success
    }
}
