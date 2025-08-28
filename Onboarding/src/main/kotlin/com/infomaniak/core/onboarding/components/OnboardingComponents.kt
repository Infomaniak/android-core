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
package com.infomaniak.core.onboarding.components

import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.infomaniak.core.compose.margin.Margin

/**
 * A few helper components for when you need to create an onboarding with simple specs. Using these components inside of
 * [com.infomaniak.core.onboarding.OnboardingScaffold] makes it faster to create an onboarding and helps you ensure you have the
 * correct styling used across different apps.
 *
 * When using the cross app login module, there's also a CrossLoginBottomContent component to easily define the bottom content of
 * [com.infomaniak.core.onboarding.OnboardingScaffold].
 */
object OnboardingComponents {
    /**
     * Easy to reuse way of displaying the background behind HorizontalPager's every page. This should be enough for all
     * situations as all backgrounds should consist of simple vector images.
     */
    @Composable
    fun DefaultBackground(background: ImageVector, modifier: Modifier = Modifier) {
        Image(
            imageVector = background,
            contentDescription = null,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
    }

    /**
     * Easy to reuse way of displaying a simple Lottie animation. When on the same screen, the animation will play once and then stop
     * forever and not loop. The animation will restart from zero when leaving the page and coming back to it.
     *
     * @param lottieRawRes the lottie animation to display.
     * @param isCurrentPageVisible whether or not the current page is presented to the user. This makes it so the animation only
     * starts playing when the page is presented to the user and not while it's loaded but outside of the screen. This also makes so
     * the animation can start again when the user goes back to a previously seen page.
     *
     * Example usage:
     * ```
     * DefaultLottieIllustration(
     *     LottieCompositionSpec.RawRes(R.raw.storage_cardboard_box_pile),
     *     pagerState.currentPage == illustrationIndex
     * )
     * ```
     */
    @Composable
    fun DefaultLottieIllustration(@RawRes lottieRawRes: Int, isCurrentPageVisible: () -> Boolean, modifier: Modifier = Modifier) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRawRes))

        // We have to specify the isPlaying parameter in order to play the animation only when the page is selected.
        // Otherwise, the ViewPager can load the page and start the animation before it's visible.
        LottieAnimation(composition, restartOnPlay = true, isPlaying = isCurrentPageVisible(), modifier = modifier)
    }

    @Composable
    fun RepeatableLottieIllustration(
        @RawRes lottieRawRes: Int,
        isCurrentPageVisible: () -> Boolean,
        firstFrame: Int,
        lastFrame: Int,
        modifier: Modifier = Modifier
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRawRes))

        // We have to specify the isPlaying parameter in order to play the animation only when the page is selected.
        // Otherwise, the ViewPager can load the page and start the animation before it's visible.
        val firstRunAnimationProgress by animateLottieCompositionAsState(composition, isPlaying = isCurrentPageVisible())
        val repeatedAnimationProgress by animateLottieCompositionAsState(
            composition = composition,
            isPlaying = isCurrentPageVisible(),
            clipSpec = LottieClipSpec.Frame(min = firstFrame, max = lastFrame),
        )

        var playbackMode: PlaybackMode by rememberSaveable { mutableStateOf(PlaybackMode.FirstRun) }

        LaunchedEffect(firstRunAnimationProgress) {
            if (firstRunAnimationProgress < 1f) return@LaunchedEffect
            playbackMode = PlaybackMode.Repeating
        }

        LottieAnimation(
            composition = composition,
            progress = {
                when (playbackMode) {
                    PlaybackMode.FirstRun -> firstRunAnimationProgress
                    PlaybackMode.Repeating -> repeatedAnimationProgress
                }
            },
            modifier = modifier,
        )
    }

    private enum class PlaybackMode {
        FirstRun, Repeating
    }

    /**
     * Easy to reuse way of displaying the title and description of an onboarding page when you don't need anything fancy. Using
     * this component ensures you have the correct styling like other onboardings of other apps.
     */
    @Composable
    fun DefaultTitleAndDescription(
        title: String,
        description: String,
        titleStyle: TextStyle,
        descriptionStyle: TextStyle,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = Margin.Large)
                .widthIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(Margin.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(textAlign = TextAlign.Center, text = title, style = titleStyle)
            Text(textAlign = TextAlign.Center, text = description, style = descriptionStyle)
        }
    }
}
