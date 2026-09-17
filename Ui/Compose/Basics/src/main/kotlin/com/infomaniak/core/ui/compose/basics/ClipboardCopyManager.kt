/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.ui.compose.basics

import android.content.ClipData
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ClipboardCopyManager(
    private val coroutineScope: CoroutineScope,
    private val clipboard: Clipboard,
    private val onDisplayUserFeedback: (String) -> Unit,
) {
    fun copy(text: String, feedbackMessage: String) {
        coroutineScope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(text, text)))

            if (SDK_INT < 33) onDisplayUserFeedback(feedbackMessage)
        }
    }
}

@Composable
fun rememberClipboardCopyManager(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onDisplayUserFeedback: ((String) -> Unit)? = null,
): ClipboardCopyManager {
    val clipboard = LocalClipboard.current

    val onFeedback = onDisplayUserFeedback ?: run {
        val context = LocalContext.current
        { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    return remember(clipboard, onFeedback, coroutineScope) {
        ClipboardCopyManager(coroutineScope, clipboard, onFeedback)
    }
}
