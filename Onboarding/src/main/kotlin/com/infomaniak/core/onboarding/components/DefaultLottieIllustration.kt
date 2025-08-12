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
package com.infomaniak.core.onboarding.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Easy to reuse way of displaying a simple Lottie animation. When on the same screen, the animation will play once and then stop
 * forever and not loop. The animation will restart from zero when leaving the page and coming back to it.
 *
 * @param lottieCompositionSpec the lottie animation to display.
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
// This uses a generic <T : LottieCompositionSpec> instead of an instance of LottieCompositionSpec as argument because the
// animation breaks on SwissTransfer when swiping between illustrations when providing the parent class instead of a specific
// child class. This makes no sense.
@Composable
fun <T : LottieCompositionSpec> DefaultLottieIllustration(lottieCompositionSpec: T, isCurrentPageVisible: () -> Boolean) {
    val composition by rememberLottieComposition(lottieCompositionSpec)

    // We have to specify the isPlaying parameter in order to play the animation only when the page is selected.
    // Otherwise, the ViewPager can load the page and start the animation before it's visible.
    LottieAnimation(composition, restartOnPlay = true, isPlaying = isCurrentPageVisible())
}
