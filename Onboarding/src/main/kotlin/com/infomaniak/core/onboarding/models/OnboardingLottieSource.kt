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
package com.infomaniak.core.onboarding.models

import com.lottiefiles.dotlottie.core.util.DotLottieSource

sealed class OnboardingLottieSource {
    data class Url(val urlString: String) : OnboardingLottieSource() {  // .json / .lottie
        override fun toDotLottieSource(): DotLottieSource = DotLottieSource.Url(urlString)
    }

    data class Asset(val assetPath: String) : OnboardingLottieSource() {  // .json / .lottie
        override fun toDotLottieSource(): DotLottieSource = DotLottieSource.Asset(assetPath)
    }

    data class Res(val resourceId: Int) : OnboardingLottieSource() {  // .json / .lottie from raw resources
        override fun toDotLottieSource(): DotLottieSource = DotLottieSource.Res(resourceId)
    }

    data class Data(val data: ByteArray) : OnboardingLottieSource() {
        override fun toDotLottieSource(): DotLottieSource = DotLottieSource.Data(data)
    }

    data class Json(val jsonString: String) : OnboardingLottieSource() {
        override fun toDotLottieSource(): DotLottieSource = DotLottieSource.Json(jsonString)
    }

    internal abstract fun toDotLottieSource(): DotLottieSource
}
