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
package com.infomaniak.core.privacymanagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.privacymanagement.theme.LocalPrivacyManagementTheme
import com.infomaniak.core.privacymanagement.tracker.Tracker
import com.infomaniak.core.ui.compose.margin.Margin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.infomaniak.core.common.R as RCore

@Composable
fun PrivacyManagementHomeContent(
    trackerList: ImmutableList<Tracker>,
    modifier: Modifier = Modifier,
    onTrackerClick: (Tracker) -> Unit,
    header: @Composable () -> Unit = {},
    divider: @Composable () -> Unit = {},
    rightIcon: @Composable (() -> Unit) = {},
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        header()
        Text(
            text = stringResource(RCore.string.trackingManagementDescription),
            modifier = Modifier.padding(Margin.Medium),
        )
        Spacer(Modifier.height(Margin.Medium))
        TrackerList(
            trackerList = trackerList,
            divider = divider,
            rightIcon = rightIcon,
            onTrackerClick = onTrackerClick
        )
    }
}

@Composable
private fun TrackerList(
    trackerList: ImmutableList<Tracker>,
    onTrackerClick: (Tracker) -> Unit,
    divider: @Composable () -> Unit = {},
    rightIcon: @Composable (() -> Unit) = {}
) {
    val privacyManagementTheme = LocalPrivacyManagementTheme.current
    Column(
        modifier = Modifier
            .padding(privacyManagementTheme.trackerContainerPadding)
            .clip(privacyManagementTheme.trackerContainerShape)
    ) {
        trackerList.forEachIndexed { index, tracker ->
            Button(
                onClick = { onTrackerClick(tracker) },
                colors = ButtonDefaults.buttonColors(
                    contentColor = privacyManagementTheme.trackerContainerContentColor,
                    containerColor = privacyManagementTheme.trackerContainerColor,
                ),
                shape = RoundedCornerShape(0),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Margin.Medium, vertical = Margin.Medium),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(imageVector = tracker.icon(), contentDescription = null)
                    Spacer(Modifier.width(Margin.Medium))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = tracker.label
                    )
                    rightIcon()
                }
            }
            if (index < trackerList.lastIndex) {
                divider()
            }
        }
    }
}

@Composable
@Preview
fun PrivacyManagementHomeContentPreview() {
    MaterialTheme {
        Scaffold { padding ->
            PrivacyManagementHomeContent(
                modifier = Modifier.padding(padding),
                trackerList = persistentListOf(Tracker.Sentry, Tracker.Matomo),
                header = {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.LightGray)
                    ) {
                        Text("Illustration")
                    }
                },
                divider = {
                    HorizontalDivider()
                },
                rightIcon = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.LightGray)
                    )
                },
                onTrackerClick = {}
            )
        }
    }
}
