/*
 * Infomaniak Authenticator - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.privacymanagement.images.illus.matomoLogo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.infomaniak.core.privacymanagement.images.Illus
import com.infomaniak.core.ui.compose.preview.PreviewLightAndDark
import com.infomaniak.core.ui.compose.theme.ThemedImage

@Suppress("UnusedReceiverParameter")
val Illus.MatomoLogo: ThemedImage
    get() = _matomo ?: object : ThemedImage {
        override val light = Illus.MatomoLogoLight
        override val dark = Illus.MatomoLogoDark
    }.also { _matomo = it }

private var _matomo: ThemedImage? = null

@PreviewLightAndDark
@Composable
private fun Preview() {
    MaterialTheme {
        Box {
            Image(
                imageVector = Illus.MatomoLogo.image(),
                contentDescription = null,
                modifier = Modifier.size(Illus.previewSize),
            )
        }
    }
}
