/*
 * Infomaniak SwissTransfer - Android
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.core.R
import com.infomaniak.core.compose.basicbutton.BasicButton
import com.infomaniak.core.compose.basics.ButtonStyle
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.basics.bottomsheet.ThemedBottomSheetScaffold
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossapplogin.back.ExternalAccount
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

// TODO: Kdoc
@Suppress("UnusedReceiverParameter")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingComponents.CrossLoginBottomContent(
    modifier: Modifier = Modifier,
    accounts: () -> List<ExternalAccount>,
    skippedIds: () -> Set<Long>,
    titleColor: Color, // TODO: Extract once we have a design system and shared color tokens
    descriptionColor: Color, // TODO: Extract once we have a design system and shared color tokens
    isLastPage: () -> Boolean,
    onGoToNextPage: () -> Unit,
    onLogin: () -> Unit,
    onContinueWithSelectedAccounts: () -> Unit,
    onCreateAccount: () -> Unit,
    onAnotherAccountClicked: () -> Unit,
    onSaveSkippedAccounts: (Set<Long>) -> Unit,
    nextButtonShape: Shape = CrossLoginBottomContentDefaults.buttonShape,
    primaryButtonShape: Shape = CrossLoginBottomContentDefaults.buttonShape,
    primaryButtonHeight: Dp = CrossLoginBottomContentDefaults.primaryButtonHeight,
    accountsBottomSheetCustomization: CrossLoginCustomization = CrossLoginDefaults.customize(),
) {
    var showAccountsBottomSheet by rememberSaveable { mutableStateOf(false) }
    val localSkipped by remember { derivedStateOf { mutableStateSetOf(*skippedIds().toTypedArray()) } }
    val scope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        SharedTransitionLayout {
            AnimatedContent(
                isLastPage()
            ) { isLastPage ->
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
                                buttonStyle = object : ButtonStyle {
                                    override val height: Dp = primaryButtonHeight
                                    override val shape: Shape = primaryButtonShape
                                }
                            ),
                            onClick = { showAccountsBottomSheet = true },
                        )
                    }

                    if (isLastPage) {
                        ButtonExpanded(
                            text = if (accounts().isEmpty()) {
                                stringResource(RCross.string.buttonLogin)
                            } else {
                                pluralStringResource(
                                    RCross.plurals.buttonContinueWithAccounts,
                                    accounts().size - skippedIds().size
                                )
                            },
                            shape = primaryButtonShape,
                            modifier = Modifier
                                .sharedElement(
                                    rememberSharedContentState(key = ANIMATED_BUTTON_KEY),
                                    animatedVisibilityScope = this@AnimatedContent
                                )
                                .height(primaryButtonHeight),
                            onClick = if (accounts().isEmpty()) onLogin else onContinueWithSelectedAccounts,
                        )
                    } else {
                        ButtonNext(
                            onClick = onGoToNextPage,
                            shape = nextButtonShape,
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = ANIMATED_BUTTON_KEY),
                                animatedVisibilityScope = this@AnimatedContent
                            )
                        )
                    }

                    if (isLastPage && accounts().isEmpty()) {
                        Button(
                            modifier = Modifier.height(56.dp),
                            onClick = onCreateAccount,
                            colors = ButtonDefaults.textButtonColors()
                        ) { Text("Create an account") }
                    }
                }
            }
        }
    }

    if (showAccountsBottomSheet) {
        val sheetState = rememberModalBottomSheetState()
        ThemedBottomSheetScaffold(sheetState = sheetState, onDismissRequest = { showAccountsBottomSheet = false }) {
            CrossLoginListAccounts(
                accounts = accounts,
                skippedIds = { localSkipped },
                onAccountClicked = { accountId: Long ->
                    if (accountId in localSkipped) localSkipped -= accountId else localSkipped += accountId
                },
                onAnotherAccountClicked = onAnotherAccountClicked,
                onSaveClicked = {
                    onSaveSkippedAccounts(localSkipped)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAccountsBottomSheet = false }
                },
                customization = accountsBottomSheetCustomization,
            )
        }
    }
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
private fun ButtonExpanded(text: String, shape: Shape, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
    val buttonShape = RoundedCornerShape(16.dp)
    val primaryButtonHeight = 56.dp
}

@Preview
@Composable
private fun Preview(@PreviewParameter(AccountsPreviewParameter::class) accounts: List<ExternalAccount>) {
    MaterialTheme {
        Surface {
            OnboardingComponents.CrossLoginBottomContent(
                accounts = { accounts },
                skippedIds = { emptySet() },
                titleColor = Color.Black,
                descriptionColor = Color.Gray,
                isLastPage = { true },
                onGoToNextPage = {},
                onLogin = {},
                onContinueWithSelectedAccounts = {},
                onCreateAccount = {},
                onAnotherAccountClicked = {},
                onSaveSkippedAccounts = {},
            )
        }
    }
}
