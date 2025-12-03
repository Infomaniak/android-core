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
package com.infomaniak.core.crossapplogin.back.internal.certificates

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import java.security.MessageDigest
import android.content.pm.Signature as SigningCertificate

internal class AppSigningCertificates(
    builderAction: MutableMap<String, Set<LazyAppSigningCertificate>>.() -> Unit
) {

    val packageNames: Set<String>

    private val certificatesMapForPackages: Map<String, Set<LazyAppSigningCertificate>> = buildMap(builderAction)

    init {
        packageNames = certificatesMapForPackages.keys
    }

    suspend fun matches(targetPackageName: String, signingCertificate: SigningCertificate): Boolean {
        val acceptedCertificates = certificatesMapForPackages[targetPackageName]
            ?: certificatesMapForPackages[WILD_CARD]
            ?: return false
        return Dispatchers.Default {
            val givenSha256 = signingCertificate.sha256()
            acceptedCertificates.any { it.matches(givenSha256) }
        }
    }

    private fun SigningCertificate.sha256(): ByteArray = MessageDigest.getInstance("SHA-256").digest(toByteArray())

    companion object {
        const val WILD_CARD = "*"
    }
}
