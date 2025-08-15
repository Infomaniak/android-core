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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.infomaniak.core.R
import com.infomaniak.core.compose.basicbutton.BasicButton
import com.infomaniak.core.compose.basics.ButtonStyle
import com.infomaniak.core.compose.basics.Typography
import com.infomaniak.core.compose.margin.Margin
import com.infomaniak.core.crossapplogin.back.ExternalAccount
import com.infomaniak.core.crossapplogin.front.data.CrossLoginDefaults
import com.infomaniak.core.crossapplogin.front.icons.ArrowRight
import com.infomaniak.core.crossapplogin.front.views.components.CrossLoginSelectAccounts
import com.infomaniak.core.onboarding.components.OnboardingComponents
import com.infomaniak.core.crossapplogin.front.R as RCross

private const val ANIMATED_BUTTON_KEY = "ANIMATED_BUTTON_KEY"
private val FAB_SIZE = 64.dp

// TODO: Kdoc
@Suppress("UnusedReceiverParameter")
@OptIn(ExperimentalSharedTransitionApi::class)
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
    // Bottom sheet is not shared through the CrossLoginBottomContent because each app has its own style of bottom sheet defined
    // at the app's level
    onOpenAccountsBottomSheet: () -> Unit,
    onCreateAccount: () -> Unit,
    nextButtonShape: Shape = CrossLoginBottomContentDefaults.buttonShape,
    primaryButtonShape: Shape = CrossLoginBottomContentDefaults.buttonShape,
    primaryButtonHeight: Dp = CrossLoginBottomContentDefaults.primaryButtonHeight,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
    ) {
        SharedTransitionLayout {
            AnimatedContent(
                isLastPage()
            ) { isLastPage ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isLastPage && accounts().isNotEmpty()) {
                        CrossLoginSelectAccounts(
                            accounts = accounts,
                            skippedIds = skippedIds,
                            onClick = onOpenAccountsBottomSheet,
                            customization = CrossLoginDefaults.customize(
                                colors = CrossLoginDefaults.colors(titleColor = titleColor, descriptionColor = descriptionColor),
                                buttonStyle = object : ButtonStyle {
                                    override val height: Dp = primaryButtonHeight
                                    override val shape: Shape = primaryButtonShape
                                }
                            )
                        )
                    }

                    if (isLastPage) {
                        ButtonExpanded(
                            text = if (accounts().isEmpty()) {
                                stringResource(RCross.string.buttonLogin)
                            } else {
                                pluralStringResource(RCross.plurals.buttonContinueWithAccounts, accounts().size - skippedIds().size)
                            },
                            shape = primaryButtonShape,
                            modifier = Modifier.Companion
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
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ButtonNext(onClick: () -> Unit, shape: Shape, modifier: Modifier = Modifier.Companion) {
    val buttonWidth = FAB_SIZE
    val buttonHeight = FAB_SIZE

    BasicButton(
        modifier = modifier
            .height(buttonHeight)
            .padding(horizontal = Margin.Medium)
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
private fun ButtonExpanded(text: String, shape: Shape, modifier: Modifier = Modifier.Companion, onClick: () -> Unit) {
    var visibility by rememberSaveable { mutableFloatStateOf(0f) }

    val textVisibility by animateFloatAsState(
        targetValue = visibility,
        animationSpec = tween(),
        label = "Onboarding expanded button text visibility",
    )

    LaunchedEffect(Unit) { visibility = 1f }

    BasicButton(
        modifier = modifier
            .padding(horizontal = Margin.Medium)
            .width(400.dp),
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
