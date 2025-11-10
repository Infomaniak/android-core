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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.infomaniak.core.compose.basicbutton.BasicButton
import com.infomaniak.core.compose.basicbutton.BasicButtonDelay
import com.infomaniak.core.compose.basics.ButtonStyle
import com.infomaniak.core.compose.basics.Dimens
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.basics.bottomsheet.ThemedBottomSheetScaffold
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel.AccountCheckingStatus
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel.AccountsCheckingState
import com.infomaniak.core.crossapplogin.back.BaseCrossAppLoginViewModel.Companion.filterSelectedAccounts
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.crossapplogin.back.newSkippedAccountIdsToKeepSingleSelection
import com.infomaniak.core.crossapplogin.front.R
import com.infomaniak.core.crossapplogin.front.data.CrossLoginCustomization
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crossapplogin.front.icons.ArrowRight
import com.infomaniak.core.crossapplogin.front.previews.AccountsPreviewParameter
import com.infomaniak.core.crossapplogin.front.views.components.CrossLoginListAccounts
import com.infomaniak.core.crossapplogin.front.views.components.CrossLoginSelectAccounts
import com.infomaniak.core.onboarding.components.OnboardingComponents
import kotlinx.coroutines.launch

private const val ANIMATED_BUTTON_KEY = "ANIMATED_BUTTON_KEY"
private val FAB_SIZE = 64.dp

/**
 * Easy to reuse way of displaying the bottom content of an [com.infomaniak.core.onboarding.OnboardingScaffold] when using the
 * cross app login.
 *
 * @param pagerState The pager state of the [com.infomaniak.core.onboarding.OnboardingScaffold]. Used to automatically detect if
 * we're at the last page and to navigate between page shen the arrow button is clicked.
 * @param accountsCheckingState The status of the accounts check. Used to know when displaying loader, the account or an error.
 * @param skippedIds The list of ids of accounts that are not currently selected by the user.
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
 * @param customization Use it to style differently the buttons and the content of the bottom sheet that lets the user select
 * what accounts to use for cross app login.
 */
@Suppress("UnusedReceiverParameter")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingComponents.CrossLoginBottomContent(
    pagerState: PagerState,
    accountsCheckingState: () -> AccountsCheckingState,
    skippedIds: () -> Set<Long>,
    onLogin: () -> Unit,
    onContinueWithSelectedAccounts: (List<ExternalAccount>) -> Unit,
    onCreateAccount: () -> Unit,
    onUseAnotherAccountClicked: () -> Unit,
    onSaveSkippedAccounts: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    isSingleSelection: Boolean = false,
    isLoginButtonLoading: () -> Boolean = { false },
    isSignUpButtonLoading: () -> Boolean = { false },
    nextButtonShape: Shape = CrossLoginBottomContentDefaults.nextButtonShape,
    customization: CrossLoginCustomization = CrossLoginDefaults.customize(),
) {
    var showAccountsBottomSheet by rememberSaveable { mutableStateOf(false) }
    val isLastPage by remember { derivedStateOf { pagerState.currentPage >= pagerState.pageCount - 1 } }

    val scope = rememberCoroutineScope()

    val accounts by remember { derivedStateOf { accountsCheckingState().checkedAccounts } }

    val shouldLoadAccount by remember {
        derivedStateOf {
            val state = accountsCheckingState()
            state.checkedAccounts().isEmpty() && state.status is AccountCheckingStatus.Ongoing
        }
    }

    val isCheckingComplete by remember { derivedStateOf { accountsCheckingState().status is AccountCheckingStatus.Complete } }

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
                    val animatedButtonModifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = ANIMATED_BUTTON_KEY),
                        animatedVisibilityScope = this@AnimatedContent,
                    )

                    if (isLastPage) {
                        if (shouldLoadAccount) {
                            CrossAppLoginAccountsLoader(customization)
                        } else {
                            val shouldDisplayCrossLogin = accounts().isNotEmpty()

                            if (shouldDisplayCrossLogin) {
                                CrossLoginSelectAccounts(
                                    accounts = accounts,
                                    skippedIds = skippedIds,
                                    customization = customization,
                                    isLoading = { !isCheckingComplete },
                                    onClick = { showAccountsBottomSheet = true },
                                )
                            }

                            ConnectionButton(
                                primaryButtonType = customization.buttonStyle,
                                accounts = accounts,
                                skippedIds = skippedIds,
                                isLoginButtonLoading = isLoginButtonLoading,
                                onLogin = onLogin,
                                onContinueWithSelectedAccounts = {
                                    onContinueWithSelectedAccounts(accounts().filterSelectedAccounts(skippedIds()))
                                },
                                modifier = animatedButtonModifier,
                            )

                            if (!shouldDisplayCrossLogin) AccountCreationButton(isSignUpButtonLoading, onCreateAccount)
                        }
                    } else {
                        ButtonNext(
                            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                            shape = nextButtonShape,
                            modifier = animatedButtonModifier,
                        )
                    }
                }
            }
        }
    }

    if (showAccountsBottomSheet) {
        val sheetState = rememberModalBottomSheetState()
        ThemedBottomSheetScaffold(sheetState = sheetState, onDismissRequest = { showAccountsBottomSheet = false }) {
            val localSkipped = rememberLocalSkippedAccountIds(accounts, skippedIds, isSingleSelection)
            CrossLoginListAccounts(
                accounts = accounts,
                skippedIds = { localSkipped },
                isLoading = { !isCheckingComplete },
                onAccountClicked = { accountId ->
                    when {
                        isSingleSelection -> {
                            localSkipped.addAll(accounts().map { it.id })
                            localSkipped.remove(accountId)
                        }
                        accountId in localSkipped -> localSkipped -= accountId
                        else -> localSkipped += accountId
                    }
                },
                onAnotherAccountClicked = onUseAnotherAccountClicked,
                onSaveClicked = {
                    onSaveSkippedAccounts(localSkipped)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAccountsBottomSheet = false }
                },
                customization = customization,
            )
        }
    }
}

@Composable
private fun CrossAppLoginAccountsLoader(customization: CrossLoginCustomization) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Margin.Medium),
    ) {
        Text(
            text = stringResource(R.string.crossAppLoginAccountsLoaderTitle),
            textAlign = TextAlign.Center,
            style = Typography.bodyRegular,
            color = customization.colors.titleColor,
        )
        CircularProgressIndicator(Modifier.width(Dimens.iconSize))
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

    if (isSingleSelection) {
        LaunchedEffect(accounts, skippedIds) {
            val newSet = newSkippedAccountIdsToKeepSingleSelection(accounts, localSkipped)
            localSkipped.addAll(newSet)
            localSkipped.retainAll(newSet) // Drop elements not in newSet
        }
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
private fun ConnectionButton(
    primaryButtonType: ButtonStyle,
    accounts: () -> List<ExternalAccount>,
    skippedIds: () -> Set<Long>,
    isLoginButtonLoading: () -> Boolean,
    onLogin: () -> Unit,
    onContinueWithSelectedAccounts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val getConnexionButtonText = if (accounts().isEmpty()) {
        stringResource(R.string.buttonLogin)
    } else {
        pluralStringResource(R.plurals.buttonContinueWithAccounts, accounts().size - skippedIds().size)
    }

    ButtonExpanded(
        text = getConnexionButtonText,
        shape = primaryButtonType.shape,
        isLoginButtonLoading = isLoginButtonLoading,
        onClick = if (accounts().isEmpty()) onLogin else onContinueWithSelectedAccounts,
        modifier = modifier.height(primaryButtonType.height),
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ButtonExpanded(
    text: String,
    shape: Shape,
    isLoginButtonLoading: () -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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

@Composable
private fun AccountCreationButton(isSignUpButtonLoading: () -> Boolean, onClick: () -> Unit) {
    BasicButton(
        modifier = Modifier.height(48.dp),
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(),
        showIndeterminateProgress = isSignUpButtonLoading,
        indeterminateProgressDelay = BasicButtonDelay.Delayed,
    ) {
        Text(stringResource(R.string.buttonCreateAccount), style = Typography.bodyMedium)
    }
}

object CrossLoginBottomContentDefaults {
    val nextButtonShape = RoundedCornerShape(Dimens.largeCornerRadius)
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    MaterialTheme {
        Surface {
            OnboardingComponents.CrossLoginBottomContent(
                pagerState = rememberPagerState { 1 },
                accountsCheckingState = {
                    AccountsCheckingState(AccountCheckingStatus.Ongoing, checkedAccounts = { accounts })
                },
                skippedIds = { emptySet() },
                onLogin = {},
                onContinueWithSelectedAccounts = {},
                onCreateAccount = {},
                onUseAnotherAccountClicked = {},
                onSaveSkippedAccounts = {},
                isLoginButtonLoading = { true },
                customization = CrossLoginDefaults.customize(
                    colors = CrossLoginDefaults.colors(titleColor = Color.Black, descriptionColor = Color.Gray),
                )
            )
        }
    }
}
