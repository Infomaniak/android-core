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
package com.infomaniak.core.dotlottie

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.infomaniak.core.dotlottie.model.DotLottieTheme
import com.infomaniak.core.ui.compose.materialthemefromxml.MaterialThemeFromXml
import com.lottiefiles.dotlottie.core.compose.runtime.DotLottieController
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieEventListener
import com.lottiefiles.dotlottie.core.util.DotLottieSource

class DotLottieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var animationRes by mutableStateOf<Int?>(null)
    var theme by mutableStateOf<DotLottieTheme>(DotLottieTheme.Embedded(null))

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DotLottieView, defStyleAttr, 0).apply {
            animationRes = getResourceId(R.styleable.DotLottieView_animationRes, NO_ID).takeIf { it != NO_ID }
            theme = DotLottieTheme.Embedded(getNonResourceString(R.styleable.DotLottieView_themeId))
            recycle()
        }

        val composeView = ComposeView(context).apply {
            setContent {
                MaterialThemeFromXml {
                    animationRes?.let { ViewContent(it, theme) }
                }
            }
        }

        addView(composeView)
    }
}

@Composable
private fun ViewContent(@RawRes animationRes: Int, theme: DotLottieTheme) {
    val controller = remember { DotLottieController() }

    DotLottieAnimation(
        source = DotLottieSource.Res(animationRes),
        autoplay = true,
        themeId = theme.id,
        controller = controller,
        eventListeners = listOf(
            object : DotLottieEventListener {
                override fun onLoad() {
                    if (theme is DotLottieTheme.Custom) controller.setThemeData(theme.toData())
                }
            }
        ),
    )
}
