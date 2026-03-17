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
package com.infomaniak.core.privacymanagement.tracker

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.infomaniak.core.privacymanagement.images.Illus
import com.infomaniak.core.privacymanagement.images.illus.matomoLogo.MatomoLogo
import com.infomaniak.core.privacymanagement.images.illus.sentryLogo.SentryLogo
import com.infomaniak.core.privacymanagement.images.illus.matomoLogoLabel.MatomoLogoLabel
import com.infomaniak.core.privacymanagement.images.illus.sentryLogoLabel.SentryLogoLabel
import com.infomaniak.core.common.R as RCore

enum class Tracker(
    @StringRes val titleRes: Int,
    val icon: @Composable () -> ImageVector,
    val iconWithLabel: @Composable () -> ImageVector,
    @StringRes val descriptionRes: Int,
) {
    Sentry(
        titleRes = RCore.string.trackingSentryTitle,
        icon = { Illus.SentryLogo.image() },
        iconWithLabel = { Illus.SentryLogoLabel.image() },
        descriptionRes = RCore.string.trackingSentryDescription
    ),
    Matomo(
        titleRes = RCore.string.trackingMatomoTitle,
        icon = { Illus.MatomoLogo.image() },
        iconWithLabel = { Illus.MatomoLogoLabel.image() },
        descriptionRes = RCore.string.trackingMatomoDescription
    ),
}
