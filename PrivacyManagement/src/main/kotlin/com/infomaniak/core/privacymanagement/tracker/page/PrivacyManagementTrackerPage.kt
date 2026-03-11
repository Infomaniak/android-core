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
package com.infomaniak.core.privacymanagement.tracker.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.infomaniak.core.privacymanagement.tracker.Tracker
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.common.R as RCore

@Composable
fun PrivacyManagementTrackerPage(
    tracker: Tracker,
    modifier: Modifier = Modifier,
    isTrackerEnabled: () -> Boolean,
    onTrackerSwitchClick: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = tracker.iconWithLabel(),
            contentDescription = null,
            modifier = Modifier.padding(Margin.Medium),
        )
        Text(
            text = stringResource(tracker.descriptionRes),
            modifier = Modifier.padding(Margin.Medium),
        )
        Spacer(Modifier.height(Margin.Medium))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Margin.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(RCore.string.trackingAuthorizeTracking),
            )
            Spacer(Modifier.weight(1.0f))
            Switch(
                checked = isTrackerEnabled(),
                onCheckedChange = { onTrackerSwitchClick(it) },
            )
        }
    }
}

@Composable
@Preview
fun PrivacyManagementTrackerPagePreview(
    @PreviewParameter(TrackerPreviewParameterProvider::class) tracker: Tracker
) {
    MaterialTheme {
        Scaffold { padding ->
            PrivacyManagementTrackerPage(
                modifier = Modifier.padding(padding),
                tracker = tracker,
                isTrackerEnabled = { true },
                onTrackerSwitchClick = {},
            )
        }
    }
}

class TrackerPreviewParameterProvider : PreviewParameterProvider<Tracker> {
    override val values: Sequence<Tracker> = sequenceOf(
        Tracker.Sentry,
        Tracker.Matomo,
    )
}
