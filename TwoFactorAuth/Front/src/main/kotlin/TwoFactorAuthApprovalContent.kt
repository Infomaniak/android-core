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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.basics.CallableState
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.basics.rememberCallableState
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.network.EDIT_PASSWORD_URL
import com.infomaniak.core.twofactorauth.back.AbstractTwoFactorAuthViewModel.Challenge
import com.infomaniak.core.twofactorauth.back.ConnectionAttemptInfo
import com.infomaniak.core.twofactorauth.back.RemoteChallenge
import com.infomaniak.core.twofactorauth.back.RemoteChallenge.Device.Type.Computer
import com.infomaniak.core.twofactorauth.back.RemoteChallenge.Device.Type.Phone
import com.infomaniak.core.twofactorauth.back.RemoteChallenge.Device.Type.Tablet
import com.infomaniak.core.twofactorauth.back.TwoFactorAuth.Outcome
import com.infomaniak.core.twofactorauth.front.components.Button
import com.infomaniak.core.twofactorauth.front.components.CardElement
import com.infomaniak.core.twofactorauth.front.components.CardElementPosition
import com.infomaniak.core.twofactorauth.front.components.CardKind
import com.infomaniak.core.twofactorauth.front.components.SecurityTheme
import com.infomaniak.core.twofactorauth.front.components.VirtualCardState
import com.infomaniak.core.twofactorauth.front.components.rememberVirtualCardState
import com.infomaniak.core.twofactorauth.front.components.virtualCardHost
import com.infomaniak.core.twofactorauth.front.elements.CircledIcon
import com.infomaniak.core.twofactorauth.front.elements.ShieldK
import com.infomaniak.core.twofactorauth.front.elements.lightSourceBehind
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import splitties.experimental.ExperimentalSplittiesApi
import java.net.UnknownHostException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import com.infomaniak.core.R as RCore

@Composable
fun TwoFactorAuthApprovalContent(challenge: Challenge) = Surface(Modifier.fillMaxSize()) {
    Box(Modifier, contentAlignment = Alignment.Center) {

        val virtualCardState = rememberVirtualCardState(kind = if (isSystemInDarkTheme()) CardKind.Outlined else CardKind.Normal)
        val scrollState = rememberScrollState()
        val maxWidth = 480.dp
        val cardOuterPadding = Margin.Medium
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(cardOuterPadding)
                .virtualCardHost(virtualCardState),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CloseButton(onClick = challenge.dismiss, Modifier.align(Alignment.Start))

            when (val state = challenge.state) {
                is Challenge.State.ApproveOrReject, null -> {
                    PendingChallenge(virtualCardState, maxWidth, cardOuterPadding, challenge, state)
                }
                is Challenge.State.Done -> ChallengeDone(
                    outcome = state.data,
                    close = challenge.dismiss
                )
                is Challenge.State.Issue -> ChallengeResponseIssue(
                    outcome = state.data,
                    close = challenge.dismiss,
                    retry = state.retry
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ChallengeDone(outcome: Outcome.Done, close: () -> Unit) {
    val iconResId: Int
    val titleResId: Int
    val descResId: Int
    when (outcome) {
        Outcome.Done.Success -> return // Not showing any confirmation screen, should be auto-dismissed.
        Outcome.Done.Rejected -> {
            iconResId = R.drawable.ic_hand_octagon_slash
            titleResId = R.string.twoFactorAuthConnectionRejectedTitle
            descResId = R.string.twoFactorAuthConnectionRejectedDescription
        }
        Outcome.Done.Expired -> {
            iconResId = R.drawable.ic_clock
            titleResId = R.string.twoFactorAuthExpiredErrorTitle
            descResId = R.string.twoFactorAuthExpiredErrorDescription
        }
        Outcome.Done.AlreadyProcessed -> {
            iconResId = R.drawable.ic_phone_circular_arrows_clockwise
            titleResId = R.string.twoFactorAuthAlreadyProcessedErrorTitle
            descResId = R.string.twoFactorAuthCheckOriginDescription
        }
    }
    ChallengeResponseResult(
        iconResId = iconResId,
        iconBackground = MaterialTheme.colorScheme.tertiary,
        titleResId = titleResId,
        contentText = {
            Text(text = stringResource(descResId))
        },
        close = close,
        actionButton = if (outcome == Outcome.Done.Rejected) { { EditPasswordButton() } } else null
    )
}

@Composable
private fun EditPasswordButton() {
    val uriHandler = LocalUriHandler.current
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { uriHandler.openUri(EDIT_PASSWORD_URL) },
    ) { Text(stringResource(R.string.buttonEditPassword), overflow = TextOverflow.Ellipsis, maxLines = 1) }
}

@Composable
private fun ColumnScope.ChallengeResponseIssue(
    outcome: Outcome.Issue,
    close: () -> Unit,
    retry: () -> Unit
) {
    val titleResId: Int = when (outcome) {
        is Outcome.Issue.ErrorResponse -> RCore.string.anErrorHasOccurred
        is Outcome.Issue.Network -> R.string.twoFactorAuthNoNetworkErrorTitle
        is Outcome.Issue.Unknown -> RCore.string.anErrorHasOccurred
    }
    ChallengeResponseResult(
        iconResId = when (outcome) {
            is Outcome.Issue.ErrorResponse -> R.drawable.ic_cross
            is Outcome.Issue.Network -> R.drawable.ic_antenna_slash
            is Outcome.Issue.Unknown -> R.drawable.ic_cross
        },
        iconBackground = when (outcome) {
            is Outcome.Issue.Network -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.error
        },
        titleResId = titleResId,
        contentText = {
            val textResId = when (outcome) {
                is Outcome.Issue.ErrorResponse -> R.string.twoFactorAuthGenericErrorDescription
                is Outcome.Issue.Network -> R.string.twoFactorAuthNoNetworkErrorDescription
                is Outcome.Issue.Unknown -> R.string.twoFactorAuthGenericErrorDescription
            }
            Text(text = stringResource(textResId))
        },
        close = close
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { retry() },
        ) { Text(stringResource(RCore.string.buttonRetry), overflow = TextOverflow.Ellipsis, maxLines = 1) }
    }
}

@Composable
private fun ColumnScope.ChallengeResponseResult(
    iconResId: Int,
    iconBackground: Color,
    titleResId: Int,
    contentText: @Composable () -> Unit,
    close: () -> Unit,
    actionButton: (@Composable () -> Unit)? = null
) {
    Spacer(Modifier.height(Margin.Huge))
    CircledIcon(iconResId, color = iconBackground)
    Spacer(Modifier.height(Margin.Medium))
    Text(
        text = stringResource(titleResId),
        style = Typography.h1,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(Margin.Medium))
    ProvideTextStyle(LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
        contentText()
    }
    Spacer(Modifier.weight(2f))
    actionButton?.let {
        Spacer(Modifier.height(Margin.Medium))
        it()
    }
    Spacer(Modifier.height(Margin.Medium))
    Button(
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.filledTonalButtonColors(),
        onClick = { close() },
    ) { Text(stringResource(RCore.string.buttonClose), overflow = TextOverflow.Ellipsis, maxLines = 1) }
}

@Composable
private fun ColumnScope.PendingChallenge(
    virtualCardState: VirtualCardState,
    maxWidth: Dp,
    cardOuterPadding: Dp,
    challenge: Challenge,
    state: Challenge.State.ApproveOrReject?,
) {

    Spacer(Modifier.height(Margin.Huge))

    BrandedPrompt()

    Spacer(Modifier.weight(1f))
    //TODO[issue-blocked]: Replace line below with heightIn above once this is fixed:
    // https://issuetracker.google.com/issues/294046936
    Spacer(Modifier.height(Margin.Medium))

    CardElement(
        virtualCardState,
        Modifier.lightSourceBehind(maxWidth, cardOuterPadding),
        elementPosition = CardElementPosition.First
    ) {
        Column(
            Modifier.padding(Margin.Medium),
            verticalArrangement = Arrangement.spacedBy(Margin.Medium)
        ) {
            AccountInfoContent(challenge.data.targetAccount)
            HorizontalDivider()
            InfoElement(stringResource(R.string.twoFactorAuthWhenLabel)) { TimeAgoText(challenge.attemptTimeMark) }
            InfoElement(stringResource(R.string.twoFactorAuthDeviceLabel)) { DeviceRow(challenge.data) }
            InfoElement(stringResource(R.string.twoFactorAuthLocationLabel)) { Text(challenge.data.location) }
        }
    }
    CardElement(virtualCardState, Modifier.weight(1f).fillMaxWidth())
    val confirmRequest = rememberCallableState<Boolean>()
    LaunchedEffect(state) { state?.action(confirmRequest.awaitOneCall()) }
    CardElement(virtualCardState, elementPosition = CardElementPosition.Last) {
        ConfirmOrRejectRow(confirmRequest, Modifier.padding(Margin.Medium))
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier) {
        Icon(
            Icons.Default.Close,
            contentDescription = stringResource(RCore.string.buttonClose),
        )
    }
}

@Composable
private fun DeviceRow(connectionAttemptInfo: ConnectionAttemptInfo) {
    Row(horizontalArrangement = Arrangement.spacedBy(Margin.Mini), verticalAlignment = Alignment.CenterVertically) {
        Text(connectionAttemptInfo.deviceOrBrowserName)
        connectionAttemptInfo.deviceType?.let { DeviceIcon(it) }
    }
}

@Composable
private fun DeviceIcon(type: RemoteChallenge.Device.Type) {
    val resId = when (type) {
        Phone -> R.drawable.ic_mobile
        Tablet -> R.drawable.ic_tablet
        Computer -> R.drawable.ic_computer
    }
    Icon(painterResource(resId), contentDescription = null)
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
    Spacer(Modifier.height(Margin.Medium))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Margin.Medium, alignment = Alignment.CenterHorizontally),
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
            .padding(horizontal = Margin.Huge * 1 / LocalDensity.current.fontScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Margin.Medium)
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
private fun InfoElement(label: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Margin.Micro)) {
        Text(label, style = Typography.labelRegular)
        ProvideTextStyle(Typography.bodyMedium) {
            content()
        }
    }
}

@Preview(fontScale = 1f)
@Preview(fontScale = 1.6f, locale = "fr")
@Preview(device = "id:Nexus One")
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(device = "spec:width=673dp,height=841dp")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun ConfirmLoginBottomSheetContentPreview() = SecurityTheme {
    val scope = rememberCoroutineScope()
    val confirmRequest = remember {
        CallableState<Boolean>().also { scope.launch(start = CoroutineStart.UNDISPATCHED) { it.awaitOneCall() } }
    }
    val state = Challenge.State.ApproveOrReject(confirmRequest)
    TwoFactorAuthApprovalContent(challengeForPreview(state))
}

@Preview
@Composable
private fun ConfirmLoginBottomSheetContentMinuteAgoPreview() = SecurityTheme {
    val state = Challenge.State.ApproveOrReject(CallableState())
    TwoFactorAuthApprovalContent(challengeForPreview(state, timeAgo = 1.minutes))
}

@Preview
@Composable
private fun ConfirmLoginBottomSheetContentMinutesAgoPreview() = SecurityTheme {
    val state = Challenge.State.ApproveOrReject(CallableState())
    TwoFactorAuthApprovalContent(challengeForPreview(state, timeAgo = 4.minutes))
}

@Preview(locale = "fr")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(device = "spec:width=673dp,height=841dp")
@Composable
private fun ChallengeExpiredPreview() = SecurityTheme {
    val state = Challenge.State.Done(Outcome.Done.Expired)
    TwoFactorAuthApprovalContent(challengeForPreview(state))
}

@Preview(locale = "fr")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun ChallengeAlreadyActionedPreview() = SecurityTheme {
    val state = Challenge.State.Done(Outcome.Done.AlreadyProcessed)
    TwoFactorAuthApprovalContent(challengeForPreview(state))
}

@Preview(locale = "fr")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun ChallengePromptCredentialsUpdatePreview() = SecurityTheme {
    val state = Challenge.State.Done(Outcome.Done.Rejected)
    TwoFactorAuthApprovalContent(challengeForPreview(state))
}

@Preview(locale = "fr")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun ChallengeResponseIssuePreview() = SecurityTheme {
    val state = Challenge.State.Issue(Outcome.Issue.ErrorResponse(503), retry = {})
    TwoFactorAuthApprovalContent(challengeForPreview(state))
}

@Preview(locale = "fr")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun ChallengeNetworkIssuePreview() = SecurityTheme {
    val state = Challenge.State.Issue(Outcome.Issue.Network(UnknownHostException()), retry = {})
    TwoFactorAuthApprovalContent(challengeForPreview(state))
}

fun challengeForPreview(
    state: Challenge.State?,
    timeAgo: Duration = 57.seconds,
    dismiss: () -> Unit = {},
): Challenge {
    return Challenge(
        data = ConnectionAttemptInfo(
            targetAccount = ConnectionAttemptInfo.TargetAccount(
                avatarUrl = "https://picsum.photos/id/140/200/200",
                fullName = "Ellen Ripley",
                initials = "ER",
                email = "ellen.ripley@domaine.ch",
                id = 2,
            ),
            deviceOrBrowserName = "Google Pixel Tablet",
            deviceType = Tablet,
            location = "Genève, Suisse",
        ),
        attemptTimeMark = TimeSource.Monotonic.markNow() - timeAgo,
        dismiss = dismiss,
        state = state,
    )
}
