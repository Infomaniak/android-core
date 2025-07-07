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

import android.app.Application
import android.content.Context
import com.infomaniak.core.network.api.ApiController
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.android.fragment.FragmentLifecycleIntegration
import io.sentry.android.fragment.FragmentLifecycleState

object SentryConfig {
    fun Application.configureSentry(
        isDebug: Boolean,
        isSentryTrackingEnabled: Boolean,
        isErrorException: (Throwable?) -> Boolean
    ) {
        SentryAndroid.init(this) { options: SentryAndroidOptions ->
            // Register the callback as an option
            options.beforeSend = SentryOptions.BeforeSendCallback { event: SentryEvent, _: Any? ->
                /**
                 * Reasons to discard Sentry events :
                 * - Application is in Debug mode
                 * - User deactivated Sentry tracking in DataManagement settings
                 * - The exception was an [ApiController.NetworkException], and we don't want to send them to Sentry
                 * - App specific exceptions defined when the method is called
                 */
                when {
                    shouldBeDiscarded(event, isDebug, isSentryTrackingEnabled, isErrorException) -> null
                    else -> event
                }
            }
            options.addIntegration(
                FragmentLifecycleIntegration(
                    application = this,
                    filterFragmentLifecycleBreadcrumbs = setOf(
                        FragmentLifecycleState.CREATED,
                        FragmentLifecycleState.STARTED,
                        FragmentLifecycleState.RESUMED,
                        FragmentLifecycleState.PAUSED,
                        FragmentLifecycleState.STOPPED,
                        FragmentLifecycleState.DESTROYED,
                    ),
                    enableAutoFragmentLifecycleTracing = true,
                )
            )
        }
    }

    fun shouldBeDiscarded(
        event: SentryEvent,
        isDebug: Boolean,
        isSentryTrackingEnabled: Boolean,
        isErrorException: (Throwable?) -> Boolean
    ): Boolean {
        val exception = event.throwable
        return isDebug
                || !isSentryTrackingEnabled
                || exception is ApiController.NetworkException
                || isErrorException(exception)
    }
}
