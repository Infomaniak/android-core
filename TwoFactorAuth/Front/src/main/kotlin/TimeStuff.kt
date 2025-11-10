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
@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core.twofactorauth.front

import android.content.res.Resources
import android.text.format.DateUtils
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.infomaniak.core.isStartedFlow
import com.infomaniak.core.time.timeToNextMinute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import splitties.coroutines.repeatWhileActive
import splitties.experimental.ExperimentalSplittiesApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import com.infomaniak.core.twofactorauth.back.R as RBack

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

@Composable
fun TimeAgoText(timeMark: TimeMark) {
    val text by timeMark.minutesAgoState()
    Text(text)
}

@Composable
fun TimeMark.minutesAgoState(): State<String> {
    var now = elapsedNow()
    val resources = LocalResources.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return produceState(initialValue = now.elapsedTimePerMinuteFormatted(resources), key1 = this) {
        lifecycle.isStartedFlow().collectLatest { isStarted ->
            if (isStarted) repeatWhileActive {
                now = elapsedNow()
                value = now.elapsedTimePerMinuteFormatted(resources)
                delay(now.timeToNextMinute())
            }
        }
    }
}

fun Duration.elapsedTimePerMinuteFormatted(resources: Resources): String = when (inWholeMinutes) {
    0L -> resources.getString(RBack.string.twoFactorAuthJustNowLabel)
    else -> DateUtils.getRelativeTimeSpanString(
        /* time = */ 0L,
        /* now = */ inWholeMilliseconds,
        /* minResolution = */ 1.minutes.inWholeMilliseconds,
    ).toString()
}
