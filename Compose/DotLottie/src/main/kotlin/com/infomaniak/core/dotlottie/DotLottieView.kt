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
import com.infomaniak.core.compose.materialthemefromxml.MaterialThemeFromXml
import com.infomaniak.core.dotlottie.model.DotLottieTheme
import com.lottiefiles.dotlottie.core.compose.runtime.DotLottieController
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieEventListener
import com.lottiefiles.dotlottie.core.util.DotLottieSource


class DotLottieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var theme by mutableStateOf<DotLottieTheme>(DotLottieTheme.Embedded(null))

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.DotLottieView, defStyleAttr, 0)
        val animationRes = styledAttributes.getResourceId(R.styleable.DotLottieView_animationRes, NO_ID)
        styledAttributes.apply {
            theme = DotLottieTheme.Embedded(getNonResourceString(R.styleable.DotLottieView_themeId))
            recycle()
        }

        val composeView = ComposeView(context).apply {
            setContent {
                MaterialThemeFromXml {
                    ViewContent(animationRes)
                }
            }
        }

        addView(composeView)
    }

    @Composable
    private fun ViewContent(@RawRes animationRes: Int) {
        val controller = remember { DotLottieController() }

        DotLottieAnimation(
            source = DotLottieSource.Res(animationRes),
            autoplay = true,
            themeId = theme.id,
            controller = controller,
            eventListeners = listOf(
                object : DotLottieEventListener {
                    override fun onLoad() {
                        theme.let {
                            if (it is DotLottieTheme.Custom) controller.setThemeData(it.toData())
                        }
                    }
                }
            ),
        )
    }
}
