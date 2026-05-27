package com.infomaniak.core.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.derivedMediaQuery
import com.infomaniak.core.ui.compose.windowinset.isWindowMedium
import com.infomaniak.core.ui.compose.windowinset.isWindowWidthSmall

@OptIn(ExperimentalMediaQueryApi::class)
@Composable
fun rememberNavigationType(): State<NavigationType> {
    val isCompact: Boolean by derivedMediaQuery { isWindowWidthSmall() }
    val isMedium: Boolean by derivedMediaQuery { isWindowMedium() }

    return remember(isCompact, isMedium) {
        val navigationType = when {
            isCompact -> NavigationType.BottomBar
            isMedium -> NavigationType.Rail
            else -> NavigationType.Drawer
        }

        mutableStateOf(navigationType)
    }
}

enum class NavigationType {
    BottomBar, Rail, Drawer
}
