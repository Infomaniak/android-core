/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2025 Infomaniak Network SA
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

import com.infomaniak.core.crossapplogin.back.BuildConfig
import com.infomaniak.core.crossapplogin.back.internal.certificates.LazyAppSigningCertificate as LazyCertificate

internal val infomaniakAppsCertificates = AppSigningCertificates {
    if (BuildConfig.DEBUG) {
        this["com.infomaniak.drive"] = setOf(
            LazyCertificate { "3A:1E:6F:0E:0E:1D:5A:6A:C9:03:60:C8:BE:2D:12:8C:73:E7:44:6D:98:A5:14:A2:55:51:25:D7:39:14:31:F8" },
        )
        this["com.infomaniak.mail"] = setOf(
            LazyCertificate { "3A:1E:6F:0E:0E:1D:5A:6A:C9:03:60:C8:BE:2D:12:8C:73:E7:44:6D:98:A5:14:A2:55:51:25:D7:39:14:31:F8" },
        )
        this["com.infomaniak.euria.debug"] = setOf(
            LazyCertificate { "3A:1E:6F:0E:0E:1D:5A:6A:C9:03:60:C8:BE:2D:12:8C:73:E7:44:6D:98:A5:14:A2:55:51:25:D7:39:14:31:F8" },
        )
        this["com.infomaniak.euria"] = setOf(
            LazyCertificate { "3A:1E:6F:0E:0E:1D:5A:6A:C9:03:60:C8:BE:2D:12:8C:73:E7:44:6D:98:A5:14:A2:55:51:25:D7:39:14:31:F8" },
        )
        return@AppSigningCertificates
    }
    this["com.infomaniak.drive"] = setOf(
        LazyCertificate { "72:C2:E2:2D:56:BA:86:07:C4:D5:A0:95:ED:97:7B:A5:F5:D1:C6:0A:AF:39:C3:3D:E2:33:BE:77:CB:0F:37:78" },
    )
    this["com.infomaniak.mail"] = setOf(
        LazyCertificate { "54:CE:26:CB:66:CB:12:EE:E6:FF:51:04:C9:5A:C4:1C:F1:93:9D:B9:C1:83:13:13:AA:52:3D:41:D7:29:EC:C2" },
    )
    this["com.infomaniak.euria"] = setOf(
        LazyCertificate { "C3:10:73:8F:60:AC:5B:F6:65:95:53:BE:C7:21:7D:BF:24:4E:9D:4C:04:BD:7D:5B:F5:51:51:2D:22:E5:03:03" },
    )
    // TODO: Add other Infomaniak apps that are expected to support cross-app login.
}
