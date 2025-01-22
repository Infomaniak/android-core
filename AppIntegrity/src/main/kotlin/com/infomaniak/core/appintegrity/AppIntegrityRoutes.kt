/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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

object AppIntegrityRoutes {

    private val BASE_URL_V1 = "${BuildConfig.APP_INTEGRITY_BASE_URL}/1/attest"

    internal val requestChallenge = "$BASE_URL_V1/challenge"
    internal val requestApiIntegrityCheck = "$BASE_URL_V1/integrity"
    const val demo = "https://api-core.devd471.dev.infomaniak.ch/1/attest/demo"
}
