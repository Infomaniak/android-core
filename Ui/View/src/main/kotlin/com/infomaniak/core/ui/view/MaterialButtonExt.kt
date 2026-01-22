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
package com.infomaniak.core.ui.view

import android.graphics.Color
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.github.razir.progressbutton.TextChangeAnimatorParams
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout.GRAVITY_CENTER
import com.infomaniak.core.sentry.SentryLog

fun MaterialButton.showProgressCatching(color: Int? = null) {
    isClickable = false
    // showProgress stores references to views which crashes when the view is freed
    runCatching {
        showProgress {
            progressColor = color ?: Color.WHITE
            gravity = GRAVITY_CENTER
        }
    }
}

fun MaterialButton.hideProgressCatching(@StringRes text: Int) {
    isClickable = true
    // hideProgress stores references to views which crashes when the view is freed
    runCatching { hideProgress(text) }.onFailure { throwable ->
        SentryLog.w("hideProgress", "An error has occurred when hideProgress", throwable)
    }
}

fun MaterialButton.hideProgressCatching(text: String) {
    isClickable = true
    // hideProgress stores references to views which crashes when the view is freed
    runCatching { hideProgress(text) }.onFailure { throwable ->
        SentryLog.w("hideProgress", "An error has occurred when hideProgress", throwable)
    }
}

fun MaterialButton.initProgress(lifecycle: LifecycleOwner? = null, color: Int? = null) {
    lifecycle?.bindProgressButton(button = this)

    val params = color?.let {
        TextChangeAnimatorParams().apply {
            useCurrentTextColor = false
            textColor = color
            fadeInMills = 0L
            fadeOutMills = 0L
        }
    }

    params?.let(::attachTextChangeAnimator) ?: attachTextChangeAnimator()
}
