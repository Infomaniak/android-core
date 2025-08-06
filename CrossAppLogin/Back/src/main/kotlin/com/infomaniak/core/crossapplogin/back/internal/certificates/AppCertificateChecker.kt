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
package com.infomaniak.core.crossapplogin.back.internal.certificates

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal sealed class AppCertificateChecker(
    val signingCertificates: AppSigningCertificates
) {

    companion object {
        val withInfomaniakApps: AppCertificateChecker = AppCertificateCheckerImpl(
            signingCertificates = infomaniakAppsCertificates,
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )
    }

    abstract suspend fun isUidAllowed(uid: Int): Boolean

    abstract suspend fun isPackageNameAllowed(packageName: String): Boolean?
}
