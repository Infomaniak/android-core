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

import android.content.Context
import com.infomaniak.core.appintegrity.exceptions.FDroidUnsupportedIntegrityException

class AppIntegrityManager(
    @Suppress("unused") private val appContext: Context,
    @Suppress("unused") userAgent: String
) : AbstractAppIntegrityManager() {

    override fun warmUpTokenProvider(appCloudNumber: Long, onFailure: () -> Unit) {
        onFailure()
    }

    override fun requestIntegrityVerdictToken(
        requestHash: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit,
        onNullTokenProvider: (String) -> Unit,
    ) {
        onFailure()
    }

    override suspend fun requestClassicIntegrityVerdictToken(challenge: String): String {
        throw FDroidUnsupportedIntegrityException()
    }

    override suspend fun getChallenge() = throw FDroidUnsupportedIntegrityException()

    override suspend fun getApiIntegrityVerdict(
        integrityToken: String,
        packageName: String,
        targetUrl: String,
    ) = throw FDroidUnsupportedIntegrityException()

    companion object {
        const val APP_INTEGRITY_MANAGER_TAG = "App integrity manager"
        const val ATTESTATION_TOKEN_HEADER = "Ik-mobile-token"
    }
}
