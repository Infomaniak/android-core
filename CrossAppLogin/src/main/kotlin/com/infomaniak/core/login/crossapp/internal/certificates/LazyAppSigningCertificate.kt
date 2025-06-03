/*
 * Infomaniak Login - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
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

package com.infomaniak.core.login.crossapp.internal.certificates

/**
 * The [getSha256Fingerprint] lambda return value can include the colon separator (`:`),
 * and can be uppercase (this is what the Play Store returns).
 */
internal class LazyAppSigningCertificate(getSha256Fingerprint: () -> String) {

    private val sha256FingerprintString by lazy { getSha256Fingerprint().replace(":", "").lowercase() }

    @OptIn(ExperimentalStdlibApi::class)
    private val sha256Fingerprint: ByteArray by lazy { sha256FingerprintString.hexToByteArray() }

    fun matches(expectedSha256: ByteArray): Boolean = sha256Fingerprint contentEquals expectedSha256
}
