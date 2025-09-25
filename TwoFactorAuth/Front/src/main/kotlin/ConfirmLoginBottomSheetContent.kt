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

@file:OptIn(ExperimentalSplittiesApi::class, ExperimentalUuidApi::class)

package com.infomaniak.core.twofactorauth.front

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.basics.CallableState
import com.infomaniak.core.compose.basics.Dimens
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.twofactorauth.back.ConnectionAttemptInfo
import com.infomaniak.core.twofactorauth.back.RemoteChallenge
import com.infomaniak.core.twofactorauth.front.components.Button
import com.infomaniak.core.twofactorauth.front.components.CardElement
import com.infomaniak.core.twofactorauth.front.components.CardElementPosition
import com.infomaniak.core.twofactorauth.front.components.CardKind
import com.infomaniak.core.twofactorauth.front.components.SecurityTheme
import com.infomaniak.core.twofactorauth.front.components.TwoFactorAuthAvatar
import com.infomaniak.core.twofactorauth.front.components.rememberVirtualCardState
import com.infomaniak.core.twofactorauth.front.components.virtualCardHost
import com.infomaniak.core.twofactorauth.front.elements.ShieldK
import com.infomaniak.core.twofactorauth.front.elements.lightSourceBehind
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun ConfirmLoginBottomSheetContent(
    attemptTimeMark: TimeMark,
    connectionAttemptInfo: ConnectionAttemptInfo,
    confirmRequest: CallableState<Boolean>,
) = Surface(Modifier.fillMaxSize()) {
    Box(Modifier, contentAlignment = Alignment.Center) {

        val virtualCardState = rememberVirtualCardState(kind = if (isSystemInDarkTheme()) CardKind.Outlined else CardKind.Normal)
        val scrollState = rememberScrollState()
        val maxWidth = 480.dp
        val cardOuterPadding = 16.dp
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(cardOuterPadding)
                .virtualCardHost(virtualCardState),
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(Modifier.weight(1f))

            BrandedPrompt()

            Spacer(Modifier.weight(1f))
            //TODO[issue-blocked]: Replace line below with heightIn above once this is fixed:
            // https://issuetracker.google.com/issues/294046936
            Spacer(Modifier.height(16.dp))

            CardElement(
                virtualCardState,
                Modifier.lightSourceBehind(maxWidth, cardOuterPadding),
                elementPosition = CardElementPosition.First
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AccountInfoContent(connectionAttemptInfo.targetAccount)
                    HorizontalDivider()
                    InfoElement(stringResource(R.string.twoFactorAuthWhenLabel)) { TimeAgoText(attemptTimeMark) }
                    InfoElement(stringResource(R.string.twoFactorAuthDeviceLabel)) {
                        Text(connectionAttemptInfo.deviceOrBrowserName)
                        //TODO: Add device type
                    }
                    InfoElement(stringResource(R.string.twoFactorAuthLocationLabel)) { Text(connectionAttemptInfo.location) }
                }
            }
            CardElement(virtualCardState, Modifier.weight(1f).fillMaxWidth())
            CardElement(virtualCardState, elementPosition = CardElementPosition.Last) {
                ConfirmOrRejectRow(confirmRequest, Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ConfirmOrRejectRow(
    confirmRequest: CallableState<Boolean>,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = stringResource(R.string.twoFactorAuthConfirmationDescription),
        style = Typography.bodyRegular,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
    ) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = { confirmRequest(false) },
            colors = ButtonDefaults.filledTonalButtonColors(),
            enabled = { confirmRequest.isAwaitingCall },
        ) { Text(stringResource(R.string.buttonDeny), overflow = TextOverflow.Ellipsis, maxLines = 1) }
        Button(
            modifier = Modifier.weight(1f),
            onClick = { confirmRequest(true) },
            enabled = { confirmRequest.isAwaitingCall },
        ) { Text(stringResource(R.string.buttonApprove), overflow = TextOverflow.Ellipsis, maxLines = 1) }
    }
}

@Composable
private fun BrandedPrompt() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp * 1 / LocalDensity.current.fontScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShieldK()
        Text(
            text = stringResource(R.string.twoFactorAuthTryingToLogInTitle),
            style = Typography.h1,
            textAlign = TextAlign.Center
        )
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
private fun InfoElement(label: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = Typography.labelRegular)
        ProvideTextStyle(Typography.bodyMedium) {
            content()
        }
    }
}

@Preview(fontScale = 1f)
@Preview(device = "id:Nexus One")
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(device = "spec:width=673dp,height=841dp")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(fontScale = 1.6f, locale = "fr")
@Composable
private fun ConfirmLoginBottomSheetContentPreview() = SecurityTheme {
    val scope = rememberCoroutineScope()
    val confirmRequest = remember {
        CallableState<Boolean>().also { scope.launch(start = CoroutineStart.UNDISPATCHED) { it.awaitOneCall() } }
    }
    ConfirmLoginBottomSheetContent(
        attemptTimeMark = TimeSource.Monotonic.markNow() + 5.seconds - 1.minutes,
        connectionAttemptInfo = ConnectionAttemptInfo(
            targetAccount = ConnectionAttemptInfo.TargetAccount(
                avatarUrl = "https://picsum.photos/id/140/200/200",
                fullName = "Ellen Ripley",
                initials = "ER",
                email = "ellen.ripley@domaine.ch",
                id = 2,
            ),
            deviceOrBrowserName = "Google Pixel Tablet",
            deviceType = RemoteChallenge.Device.Type.Tablet,
            location = "Genève, Suisse",
        ),
        confirmRequest = confirmRequest
    )
}
