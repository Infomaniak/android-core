/*
 * Infomaniak Core - Android
 * Copyright (C) 2023-2025 Infomaniak Network SA
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
package com.infomaniak.core.legacy.utils

import android.util.Log
import io.sentry.Breadcrumb
import io.sentry.ScopeCallback
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message

object SentryLog {

    private val TAG = SentryLog::class.java.simpleName

    fun v(tag: String, msg: String, throwable: Throwable? = null) {
        val formattedMessage = formatLogMessage(tag, msg)
        Log.v(TAG, formattedMessage, throwable)
        SentryLevel.DEBUG.addBreadcrumb(formattedMessage, throwable)
    }

    fun d(tag: String, msg: String, throwable: Throwable? = null) {
        val formattedMessage = formatLogMessage(tag, msg)
        Log.d(TAG, formattedMessage, throwable)
        SentryLevel.DEBUG.addBreadcrumb(formattedMessage, throwable)
    }

    fun i(tag: String, msg: String, throwable: Throwable? = null) {
        val formattedMessage = formatLogMessage(tag, msg)
        Log.i(TAG, formattedMessage, throwable)
        SentryLevel.INFO.addBreadcrumb(formattedMessage, throwable)
    }

    fun w(tag: String, msg: String, throwable: Throwable? = null) {
        val formattedMessage = formatLogMessage(tag, msg)
        Log.w(TAG, formattedMessage, throwable)
        SentryLevel.WARNING.addBreadcrumb(formattedMessage, throwable)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null, scopeCallback: ScopeCallback? = null) {
        val formattedMessage = formatLogMessage(tag, msg)
        Log.e(TAG, formattedMessage, throwable)
        SentryLevel.ERROR.apply {
            addBreadcrumb(formattedMessage, throwable)
            captureEvent(tag, msg, throwable, scopeCallback)
        }
    }

    fun wtf(tag: String, msg: String, throwable: Throwable? = null, scopeCallback: ScopeCallback? = null) {
        val formattedMessage = formatLogMessage(tag, msg)
        Log.wtf(TAG, formattedMessage, throwable)
        SentryLevel.FATAL.apply {
            addBreadcrumb(formattedMessage, throwable)
            captureEvent(tag, msg, throwable, scopeCallback)
        }
    }

    private fun SentryLevel.captureEvent(tag: String, msg: String, throwable: Throwable?, scopeCallback: ScopeCallback?) {
        val sentryEvent = SentryEvent().apply {
            setTag("SentryLogTag", tag)
            level = this@captureEvent
            logger = TAG
            message = Message().apply { this.message = msg }
            throwable?.let(::setThrowable)
        }
        if (scopeCallback == null) Sentry.captureEvent(sentryEvent) else Sentry.captureEvent(sentryEvent, scopeCallback)
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
