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

import com.infomaniak.core.network.models.exceptions.NetworkException
import io.sentry.SentryEvent
import org.junit.Assert
import kotlin.test.Test
import com.infomaniak.core.sentry.SentryConfig

class SentrySendErrorTest {

    @Test
    fun `error discarded when the debug mode is activated`() {
        Assert.assertTrue(
            SentryConfig.shouldBeDiscarded(
                event = SentryEvent(),
                isDebug = true,
                isSentryTrackingEnabled = true,
                isFilteredException = { _ -> false })
        )
    }

    @Test
    fun `error discarded when sentry tracking is disabled`() {
        Assert.assertTrue(
            SentryConfig.shouldBeDiscarded(
                event = SentryEvent(),
                isDebug = false,
                isSentryTrackingEnabled = false,
                isFilteredException = { _ -> false })
        )
    }

    @Test
    fun `error discarded when there is an error exception`() {
        Assert.assertTrue(
            SentryConfig.shouldBeDiscarded(
                event = SentryEvent(),
                isDebug = false,
                isSentryTrackingEnabled = true,
                isFilteredException = { _ -> true })
        )
    }

    @Test
    fun `error discarded when the sentry event exception is an api network exception`() {
        val sentryEvent = SentryEvent(NetworkException())
        Assert.assertTrue(
            SentryConfig.shouldBeDiscarded(
                event = sentryEvent,
                isDebug = false,
                isSentryTrackingEnabled = true,
                isFilteredException = { _ -> false })
        )
    }

    @Test
    fun `error sent when the tracking is enabled and all others parameters are false`() {
        Assert.assertFalse(
            SentryConfig.shouldBeDiscarded(
                event = SentryEvent(),
                isDebug = false,
                isSentryTrackingEnabled = true,
                isFilteredException = { _ -> false })
        )
    }
}
