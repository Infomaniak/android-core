@file:OptIn(ExperimentalSplittiesApi::class)

package com.infomaniak.core.twofactorauth.front

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import splitties.coroutines.repeatWhileActive
import splitties.experimental.ExperimentalSplittiesApi
import com.infomaniak.core.time.timeToNextMinute
import kotlin.time.Duration
import kotlin.time.TimeMark

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
    return produceState(initialValue = now.elapsedTimePerMinuteFormatted()) {
        repeatWhileActive {
            delay(now.timeToNextMinute())
            //TODO: Race with next onStart
            now = elapsedNow()
            value = now.elapsedTimePerMinuteFormatted()
        }
    }
}

fun Duration.elapsedTimePerMinuteFormatted(): String {
    val minutes = inWholeMinutes
    return if (minutes == 0L) {
        "Just now"
    } else if (minutes < 0) {
        "Dans $minutes minutes"
    } else {
        "Il y a $minutes minutes"
    }
}
