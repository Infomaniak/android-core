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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.infomaniak.core.ui.compose.margin.Margin
import com.infomaniak.core.ui.compose.preview.PreviewSmallWindow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.infomaniak.core.common.R as RCore

@Composable
fun PrivacyManagementHomeContent(
    header: @Composable () -> Unit,
    brandReceiver: ImmutableList<BrandReceiver>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        header()
        // Image(
        //     imageVector = illustration,
        //     contentDescription = null,
        //     modifier = Modifier.padding(Margin.Medium),
        // )
        Text(
            text = stringResource(RCore.string.trackingManagementDescription),
            // style = SwissTransferTheme.typography.bodyRegular,
            // color = SwissTransferTheme.colors.primaryTextColor,
            modifier = Modifier.padding(Margin.Medium),
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin.Medium),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column {
                brandReceiver.forEach { brand ->
                    Text(
                        text = brand.brandName,
                        modifier = Modifier.padding(Margin.Medium),
                    )
                }
            }
        }
    }
}

@Composable
@PreviewSmallWindow
fun PrivacyManagementHomeContentPreview() {
    MaterialTheme {
        Scaffold { padding ->
            PrivacyManagementHomeContent(
                modifier = Modifier.padding(padding),
                brandReceiver = persistentListOf(BrandReceiver.Sentry, BrandReceiver.Matomo),
                header = {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.LightGray)
                    ) {
                        Text("Illustration")
                    }
                }
            )
        }
    }
}
