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
package com.infomaniak.core.crossapplogin.front.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.core.R
import com.infomaniak.core.compose.basicbutton.BasicButton
import com.infomaniak.core.compose.basicbutton.BasicButtonDelay
import com.infomaniak.core.compose.basics.ButtonStyle
import com.infomaniak.core.compose.basics.Dimens
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.basics.bottomsheet.ThemedBottomSheetScaffold
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.crossapplogin.back.newSkippedAccountIdsToKeepSingleSelection
import com.infomaniak.core.crossapplogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crossapplogin.front.icons.ArrowRight
import com.infomaniak.core.crossapplogin.front.previews.AccountsPreviewParameter
import com.infomaniak.core.crossapplogin.front.views.components.CrossLoginListAccounts
import com.infomaniak.core.crossapplogin.front.views.components.CrossLoginSelectAccounts
import com.infomaniak.core.onboarding.components.OnboardingComponents
import kotlinx.coroutines.launch
import com.infomaniak.core.crossapplogin.front.R as RCross

private const val ANIMATED_BUTTON_KEY = "ANIMATED_BUTTON_KEY"
private val FAB_SIZE = 64.dp

/**
 * Easy to reuse way of displaying the bottom content of an [com.infomaniak.core.onboarding.OnboardingScaffold] when using the
 * cross app login.
 *
 * @param pagerState The pager state of the [com.infomaniak.core.onboarding.OnboardingScaffold]. Used to automatically detect if
 * we're at the last page and to navigate between page shen the arrow button is clicked.
 * @param accounts The list of all available accounts that can be chosen from and used for login.
 * @param skippedIds The list of ids of accounts that are not currently selected by the user.
 * @param titleColor The primary color to use for the [CrossLoginSelectAccounts].
 * @param descriptionColor The secondary lighter color to use for the [CrossLoginSelectAccounts].
 * @param onLogin When the user has no accounts for cross app login and clicks on the button to log in a new user.
 * @param onContinueWithSelectedAccounts When the user has accounts for cross app login and clicks on the button to log them.
 * @param onCreateAccount When the user has no accounts for cross app login and clicks on the button to create a new account.
 * @param onUseAnotherAccountClicked When the user has accounts for cross app login but wants to connect a different account.
 * @param onSaveSkippedAccounts When the user has confirmed his selection of accounts to use for cross app login. The updated
 * skipped accounts need to be saved at the view model level so that when login only selected accounts are taken into accounts.
 * @param modifier Modifier for the component.
 * @param isLoginButtonLoading Whether the buttons to login are loading or not. This includes both the standard login button when
 * there's no accounts for cross app login and the confirmation button when there are accounts for cross app login. The button
 * needs to start loading so it cannot be clicked multiple times while the api calls are being sent when the user has slow internet.
 * @param isSignUpButtonLoading Whether the button to create a new account is loading or not. Same as [isLoginButtonLoading].
 * @param nextButtonShape To specify a different shape for the big button with an arrow that lets you go to the next page.
 * @param primaryButtonShape To specify a specific shape for the big primary textual buttons to match each app's unique style.
 * @param primaryButtonHeight To specify a specific height for the big primary textual buttons to match each app's unique style.
 * @param accountsBottomSheetCustomization Use it to style differently the content of the bottom sheet that lets the user select
 * what accounts to use for cross app login.
 */
@Suppress("UnusedReceiverParameter")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingComponents.CrossLoginBottomContent(
    pagerState: PagerState,
    accounts: () -> List<ExternalAccount>,
    skippedIds: () -> Set<Long>,
    titleColor: Color,
    descriptionColor: Color,
    onLogin: () -> Unit,
    onContinueWithSelectedAccounts: () -> Unit,
    onCreateAccount: () -> Unit,
    onUseAnotherAccountClicked: () -> Unit,
    onSaveSkippedAccounts: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    singleSelection: Boolean = false,
    isLoginButtonLoading: () -> Boolean = { false },
    isSignUpButtonLoading: () -> Boolean = { false },
    nextButtonShape: Shape = CrossLoginBottomContentDefaults.nextButtonShape,
    primaryButtonType: ButtonStyle = CrossLoginBottomContentDefaults.primaryButtonType,
    accountsBottomSheetCustomization: CrossLoginCustomization = CrossLoginDefaults.customize(),
) {
    var showAccountsBottomSheet by rememberSaveable { mutableStateOf(false) }
    val isLastPage by remember { derivedStateOf { pagerState.currentPage >= pagerState.pageCount - 1 } }

    val scope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        SharedTransitionLayout {
            AnimatedContent(isLastPage) { isLastPage ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Margin.Medium),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isLastPage && accounts().isNotEmpty()) {
                        CrossLoginSelectAccounts(
                            accounts = accounts,
                            skippedIds = skippedIds,
                            customization = CrossLoginDefaults.customize(
                                colors = CrossLoginDefaults.colors(titleColor = titleColor, descriptionColor = descriptionColor),
                                buttonStyle = primaryButtonType,
                            ),
                            onClick = { showAccountsBottomSheet = true },
                        )
                    }

                    if (!isLastPage) {
                        ButtonNext(
                            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                            shape = nextButtonShape,
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = ANIMATED_BUTTON_KEY),
                                animatedVisibilityScope = this@AnimatedContent,
                            )
                        )
                    } else {
                        ConnectionButtons(
                            primaryButtonType,
                            accounts,
                            skippedIds,
                            isLoginButtonLoading,
                            onLogin,
                            onContinueWithSelectedAccounts,
                            onCreateAccount,
                            isSignUpButtonLoading,
                            animatedVisibilityScope = this@AnimatedContent,
                        )
                    }
                }
            }
        }
    }

    if (showAccountsBottomSheet) {
        val sheetState = rememberModalBottomSheetState()
        ThemedBottomSheetScaffold(sheetState = sheetState, onDismissRequest = { showAccountsBottomSheet = false }) {
            val localSkipped = rememberLocalSkippedAccountIds(accounts, skippedIds, singleSelection)
            CrossLoginListAccounts(
                accounts = accounts,
                skippedIds = { localSkipped },
                onAccountClicked = { accountId ->
                    if (singleSelection) {
                        localSkipped.addAll(accounts().map { it.id })
                        localSkipped.remove(accountId)
                    } else if (accountId in localSkipped) localSkipped -= accountId else localSkipped += accountId
                },
                onAnotherAccountClicked = onUseAnotherAccountClicked,
                onSaveClicked = {
                    onSaveSkippedAccounts(localSkipped)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAccountsBottomSheet = false }
                },
                customization = accountsBottomSheetCustomization,
            )
        }
    }
}

@Composable
private fun rememberLocalSkippedAccountIds(
    accounts: () -> List<ExternalAccount>,
    skippedIds: () -> Set<Long>,
    isSingleSelection: Boolean,
): SnapshotStateSet<Long> {
    val accounts = accounts()
    val skippedIds = skippedIds()
    val localSkipped = remember { mutableStateSetOf(*skippedIds.toTypedArray()) }
    if (isSingleSelection) LaunchedEffect(accounts, skippedIds) {
        val newSet = newSkippedAccountIdsToKeepSingleSelection(accounts, localSkipped)
        localSkipped.addAll(newSet)
        localSkipped.retainAll(newSet) // Drop elements not in newSet
    }
    return localSkipped
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ButtonNext(onClick: () -> Unit, shape: Shape, modifier: Modifier = Modifier) {
    val buttonWidth = FAB_SIZE
    val buttonHeight = FAB_SIZE

    BasicButton(
        modifier = modifier
            .height(buttonHeight)
            .width(buttonWidth),
        onClick = onClick,
        shape = shape,
        contentPadding = PaddingValues(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = ArrowRight,
                contentDescription = stringResource(R.string.buttonNext),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ConnectionButtons(
    primaryButtonType: ButtonStyle,
    accounts: () -> List<ExternalAccount>,
    skippedIds: () -> Set<Long>,
    isLoginButtonLoading: () -> Boolean,
    onLogin: () -> Unit,
    onContinueWithSelectedAccounts: () -> Unit,
    onCreateAccount: () -> Unit,
    isSignUpButtonLoading: () -> Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val getConnexionButtonText = if (accounts().isEmpty()) {
        stringResource(RCross.string.buttonLogin)
    } else {
        pluralStringResource(RCross.plurals.buttonContinueWithAccounts, accounts().size - skippedIds().size)
    }

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ButtonExpanded(
            text = getConnexionButtonText,
            shape = primaryButtonType.shape,
            modifier = Modifier
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = ANIMATED_BUTTON_KEY),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .height(primaryButtonType.height),
            isLoginButtonLoading = isLoginButtonLoading,
            onClick = if (accounts().isEmpty()) onLogin else onContinueWithSelectedAccounts,
        )

        if (accounts().isEmpty()) {
            BasicButton(
                modifier = Modifier.height(48.dp),
                onClick = onCreateAccount,
                colors = ButtonDefaults.textButtonColors(),
                showIndeterminateProgress = isSignUpButtonLoading,
                indeterminateProgressDelay = BasicButtonDelay.Delayed,
            ) {
                Text(stringResource(RCross.string.buttonCreateAccount), style = Typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ButtonExpanded(
    text: String,
    shape: Shape,
    modifier: Modifier = Modifier,
    isLoginButtonLoading: () -> Boolean,
    onClick: () -> Unit,
) {
    var visibility by rememberSaveable { mutableFloatStateOf(0f) }

    val textVisibility by animateFloatAsState(
        targetValue = visibility,
        animationSpec = tween(),
        label = "Onboarding expanded button text visibility",
    )

    LaunchedEffect(Unit) { visibility = 1f }

    BasicButton(
        modifier = modifier.width(400.dp),
        onClick = onClick,
        shape = shape,
        contentPadding = PaddingValues(),
        showIndeterminateProgress = isLoginButtonLoading,
        indeterminateProgressDelay = BasicButtonDelay.Delayed,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = Typography.bodyMedium,
                modifier = Modifier.graphicsLayer { alpha = textVisibility }
            )
        }
    }
}

object CrossLoginBottomContentDefaults {
    val primaryButtonType = object : ButtonStyle {
        override val height = Dimens.buttonHeight
        override val shape = RoundedCornerShape(Dimens.largeCornerRadius)
    }
    val nextButtonShape = CircleShape
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    MaterialTheme {
        Surface {
            OnboardingComponents.CrossLoginBottomContent(
                pagerState = rememberPagerState { 1 },
                accounts = { accounts },
                skippedIds = { emptySet() },
                titleColor = Color.Black,
                descriptionColor = Color.Gray,
                onLogin = {},
                onContinueWithSelectedAccounts = {},
                onCreateAccount = {},
                onUseAnotherAccountClicked = {},
                onSaveSkippedAccounts = {},
                isLoginButtonLoading = { true }
            )
        }
    }
}
