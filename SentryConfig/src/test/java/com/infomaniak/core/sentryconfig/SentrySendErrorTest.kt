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
package com.infomaniak.core.sentryconfig

import com.infomaniak.core.network.api.ApiController
import io.sentry.SentryEvent
import org.junit.Assert
import kotlin.test.Test

class SentrySendErrorTest {

    @Test
    fun `error discarded when the debug mode is activated`(): Unit {
        Assert.assertTrue(
            SentryConfig.shouldBeDiscarded(
                event = SentryEvent(),
                isDebug = true,
                isSentryTrackingEnabled = true,
                isErrorException = { _ -> false })
        )
    }

    @Test
    fun `error discarded when sentry tracking is disabled`(): Unit {
        Assert.assertTrue(
            SentryConfig.shouldBeDiscarded(
                event = SentryEvent(),
                isDebug = false,
                isSentryTrackingEnabled = false,
                isErrorException = { _ -> false })
        )
    }

    @Test
    fun `error discarded when there is an error exception`(): Unit {
        Assert.assertTrue(
            SentryConfig.shouldBeDiscarded(
                event = SentryEvent(),
                isDebug = false,
                isSentryTrackingEnabled = true,
                isErrorException = { _ -> true })
        )
    }

    @Test
    fun `error discarded when the sentry event exception is an api network exception`(): Unit {
        val sentryEvent = SentryEvent(ApiController.NetworkException())
        Assert.assertTrue(
            SentryConfig.shouldBeDiscarded(
                event = sentryEvent,
                isDebug = false,
                isSentryTrackingEnabled = true,
                isErrorException = { _ -> false })
        )
    }

    @Test
    fun `error sent when the tracking is enabled and all others parameters are false`(): Unit {
        Assert.assertFalse(
            SentryConfig.shouldBeDiscarded(
                event = SentryEvent(),
                isDebug = false,
                isSentryTrackingEnabled = true,
                isErrorException = { _ -> false })
        )
    }
}
