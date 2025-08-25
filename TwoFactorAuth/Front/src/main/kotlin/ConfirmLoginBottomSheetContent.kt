/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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

@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core.twofactorauth.front

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.basicbutton.BasicButton
import com.infomaniak.core.compose.basics.CallableState
import com.infomaniak.core.compose.basics.Dimens
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.basics.rememberCallableState
import com.infomaniak.core.twofactorauth.front.components.TwoFactorAuthAvatar
import kotlinx.serialization.SerialName
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@Composable
fun ConfirmLoginBottomSheetContent(
    attemptTimeMark: TimeMark,
    connectionAttemptInfo: ConnectionAttemptInfo,
    confirmRequest: CallableState<Boolean>,
) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
) {
    Spacer(Modifier.height(16.dp))
    BrandedPrompt()
    Spacer(Modifier.height(16.dp))
    ConnexionAttemptCard(
        attemptTimeMark, connectionAttemptInfo
    )
    Spacer(Modifier.weight(1f))
    ConfirmOrRejectRow(confirmRequest)
    Spacer(Modifier.height(16.dp))
}

@Composable
fun ConfirmOrRejectRow(
    confirmRequest: CallableState<Boolean>,
) = Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = "Confirmer cette connexion autorisera cet appareil à accéder à votre compte Infomaniak",
        style = Typography.bodyRegular,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
    ) {
        BasicButton(
            modifier = Modifier.fillMaxWidth(.5f),
            onClick = { confirmRequest(false) },
            enabled = { confirmRequest.isAwaitingCall },
        ) { Text("Refuser") }
        BasicButton(
            modifier = Modifier.fillMaxWidth(1f),
            onClick = { confirmRequest(true) },
            enabled = { confirmRequest.isAwaitingCall },
        ) { Text("Confirmer") }
    }
}

@Composable
fun BrandedPrompt() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp * 1 / LocalDensity.current.fontScale)) {
        //TODO: Full text logo
        //TODO: Illustration icon
        Text(
            text = "Êtes-vous en train d'essayer de vous connecter ?",
            style = Typography.h1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ConnexionAttemptCard(
    attemptTimeMark: TimeMark,
    connectionAttemptInfo: ConnectionAttemptInfo,
) = ElevatedCard(Modifier.fillMaxWidth()) {
    Column(Modifier
        .fillMaxWidth()
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AccountInfoContent(connectionAttemptInfo.targetAccount)
        HorizontalDivider()
        Line("Date et heure") { TimeAgoText(attemptTimeMark) }
        Line("Appareil") { Text(connectionAttemptInfo.deviceOrBrowserName) }
        Line("Lieu") { Text("Suisse") }
    }
}

@Composable
private fun AccountInfoContent(account: ConnectionAttemptInfo.TargetAccount) = Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    TwoFactorAuthAvatar(
        modifier = Modifier
            .size(Dimens.bigAvatarSize)
            .clip(CircleShape),
        account = account
    )
    Column(Modifier.weight(1f)) {
        Text(
            text = account.fullName,
            style = Typography.bodyMedium
        )
        Text(
            text = account.email,
            style = Typography.bodyRegular
        )
    }
}

@Composable
private fun Line(label: String, content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = Typography.labelRegular)
        Spacer(Modifier.weight(1f))
        ProvideTextStyle(Typography.bodySmallMedium) {
            content()
        }
    }
}

data class ConnectionAttemptInfo(
    val targetAccount: TargetAccount,
    val deviceOrBrowserName: String,
    val deviceType: DeviceType,
    val location: String,
) {
    data class TargetAccount(
        val avatarUrl: String?,
        val fullName: String,
        val initials: String,
        val email: String,
        val id: Long,
    )

    enum class DeviceType {
        @SerialName("phone")
        Phone,
        @SerialName("tablet")
        Tablet,
        @SerialName("computer")
        Computer,
    }
}

@Preview(fontScale = 1f)
@Preview(fontScale = 1.6f)
@Composable
private fun ConfirmLoginBottomSheetContentPreview() {
    ConfirmLoginBottomSheetContent(
        attemptTimeMark = TimeSource.Monotonic.markNow() + 5.seconds - 1.minutes,
        connectionAttemptInfo = ConnectionAttemptInfo(
            targetAccount = ConnectionAttemptInfo.TargetAccount(
                avatarUrl = "https://picsum.photos/id/140/200/200",
                fullName = "John Doe",
                initials = "JD",
                email = "john.doe@ik.me",
                id = 2,
            ),
            deviceOrBrowserName = "Google Pixel Tablet",
            deviceType = ConnectionAttemptInfo.DeviceType.Tablet,
            location = "Suisse",
        ),
        confirmRequest = rememberCallableState()
    )
}
