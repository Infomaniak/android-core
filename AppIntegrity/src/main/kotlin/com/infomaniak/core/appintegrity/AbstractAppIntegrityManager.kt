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
package com.infomaniak.core.appintegrity

abstract class AbstractAppIntegrityManager {

    /**
     * This function is needed in case of standard verdict request by [requestIntegrityVerdictToken].
     * It must be called once at the initialisation because it can take a long time (up to several minutes)
     */
    abstract fun warmUpTokenProvider(appCloudNumber: Long, onFailure: () -> Unit)

    /**
     * Standard verdict request for Integrity token
     * It should protect automatically from replay attack, but for now this protection seemed to not be working
     */
    abstract fun requestIntegrityVerdictToken(
        requestHash: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit,
        onNullTokenProvider: (String) -> Unit,
    )

    /**
     * Classic verdict request for Integrity token
     *
     * This doesn't automatically protect from replay attack, thus the use of challenge/challengeId pair with our API to add this
     * layer of protection.
     *
     * ###### Can throw Integrity exceptions.
     */
    abstract suspend fun requestClassicIntegrityVerdictToken(challenge: String): String

    abstract suspend fun getChallenge(): String

    abstract suspend fun getApiIntegrityVerdict(
        integrityToken: String,
        packageName: String,
        targetUrl: String,
    ): String

    /**
     *  Only used to test App Integrity in Apps before their real backend implementation
     */
    open suspend fun callDemoRoute(mobileToken: String) = Unit
}
