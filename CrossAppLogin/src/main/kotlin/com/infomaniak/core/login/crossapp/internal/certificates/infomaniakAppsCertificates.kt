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

internal val infomaniakAppsCertificates = AppSigningCertificates {
    this["com.infomaniak.drive"] = setOf(
        LazyAppSigningCertificate { "72:C2:E2:2D:56:BA:86:07:C4:D5:A0:95:ED:97:7B:A5:F5:D1:C6:0A:AF:39:C3:3D:E2:33:BE:77:CB:0F:37:78" },
    )
    this["com.infomaniak.mail"] = setOf(
        LazyAppSigningCertificate { "54:CE:26:CB:66:CB:12:EE:E6:FF:51:04:C9:5A:C4:1C:F1:93:9D:B9:C1:83:13:13:AA:52:3D:41:D7:29:EC:C2" },
    )
    //TODO: Add other Infomaniak apps that are expected to support cross-app login.
}
