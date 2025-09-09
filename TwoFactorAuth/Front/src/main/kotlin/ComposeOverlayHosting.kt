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
package com.infomaniak.core.twofactorauth.front

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

fun ComponentActivity.addComposeOverlay(content: @Composable () -> Unit) = lifecycleScope.launch {
    hostComposeOverlay(content)
}

suspend fun Activity.hostComposeOverlay(content: @Composable () -> Unit) {
    val targetView = findViewById<ViewGroup>(android.R.id.content)
    targetView.hostComposeOverlay(content)
}

suspend fun ViewGroup.hostComposeOverlay(content: @Composable () -> Unit): Nothing {
    val container = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        setContent(content)
    }
    try {
        addView(container)
        awaitCancellation()
    } finally {
        removeView(container)
    }
}
