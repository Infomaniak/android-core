/*
 * Infomaniak Core - Android
 * Copyright (C) 2023 Infomaniak Network SA
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
package com.infomaniak.lib.core.utils

import android.util.Log
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message

object SentryLog {

    private val TAG = SentryLog::class.java.simpleName

    fun v(tag: String, msg: String, throwable: Throwable? = null) {
        Log.v(TAG, formatLogMessage(tag, msg), throwable)
        SentryLevel.DEBUG.addBreadcrumb(msg, throwable)
    }

    fun d(tag: String, msg: String, throwable: Throwable? = null) {
        Log.d(TAG, formatLogMessage(tag, msg), throwable)
        SentryLevel.DEBUG.addBreadcrumb(msg, throwable)
    }

    fun i(tag: String, msg: String, throwable: Throwable? = null) {
        Log.i(TAG, formatLogMessage(tag, msg), throwable)
        SentryLevel.INFO.addBreadcrumb(msg, throwable)
    }

    fun w(tag: String, msg: String, throwable: Throwable? = null) {
        Log.w(TAG, formatLogMessage(tag, msg), throwable)
        SentryLevel.WARNING.addBreadcrumb(msg, throwable)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        Log.e(TAG, formatLogMessage(tag, msg), throwable)
        SentryLevel.ERROR.apply {
            addBreadcrumb(msg, throwable)
            captureEvent(tag, msg, throwable)
        }
    }

    fun wtf(tag: String, msg: String, throwable: Throwable? = null) {
        Log.wtf(TAG, formatLogMessage(tag, msg), throwable)
        SentryLevel.FATAL.apply {
            addBreadcrumb(msg, throwable)
            captureEvent(tag, msg, throwable)
        }
    }

    private fun SentryLevel.captureEvent(tag: String, msg: String, throwable: Throwable?) {
        val sentryEvent = SentryEvent().apply {
            setTag("SentryLogTag", tag)
            level = this@captureEvent
            logger = TAG
            message = Message().apply { this.message = msg }
            throwable?.let(::setThrowable)
        }

        Sentry.captureEvent(sentryEvent)
    }

    private fun SentryLevel.addBreadcrumb(msg: String, throwable: Throwable?) {

        val throwableMsg = throwable?.message
        val breadCrumb = when {
            throwableMsg != null -> Breadcrumb.error(throwableMsg).apply {
                category = "exception"
            }
            else -> Breadcrumb().apply {
                level = this@addBreadcrumb
                category = TAG
                message = msg
            }
        }

        Sentry.addBreadcrumb(breadCrumb)
    }

    private fun formatLogMessage(tag: String, msg: String) = "($tag): $msg"
}
