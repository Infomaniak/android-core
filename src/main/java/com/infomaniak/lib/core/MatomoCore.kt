/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2023 Infomaniak Network SA
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
package com.infomaniak.lib.core

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.infomaniak.lib.core.utils.snakeToCamelCase
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.DownloadTracker
import org.matomo.sdk.extra.TrackHelper

interface MatomoCore {

    val Context.tracker: Tracker
    val siteId: Int

    fun Context.buildTracker(): Tracker {
        return TrackerBuilder(BuildConfig.MATOMO_URL, siteId, "AndroidTracker").build(Matomo.getInstance(this)).also {
            // Put a tracker on app installs to have statistics on the number of times the app is installed or updated
            TrackHelper.track().download().identifier(DownloadTracker.Extra.ApkChecksum(this)).with(it)
        }
    }

    fun Context.addTrackingCallbackForDebugLog() {
        tracker.addTrackingCallback { trackMe ->
            trackMe.also {
                it.toMap().forEach { entry ->
                    when (entry.key) {
                        "action_name" -> Log.d("TrackerScreen", entry.value)
                        "e_c" -> Log.d("TrackerEvent", "Category : ${entry.value}")
                        "e_n" -> Log.d("TrackerEvent", "Name     : ${entry.value}")
                        "e_a" -> Log.d("TrackerEvent", "Action   : ${entry.value}")
                        "e_v" -> Log.d("TrackerEvent", "Value    : ${entry.value}")
                    }
                }
            }
        }
    }

    fun Context.trackUserId(userId: Int) {
        applicationContext.tracker.userId = userId.toString()
    }

    //region Track screens
    fun Activity.trackScreen() {
        TrackHelper.track().screen(this).title(this::class.java.simpleName).with(applicationContext.tracker)
    }

    fun Context.trackScreen(path: String, title: String) {
        TrackHelper.track().screen(path).title(title).with(applicationContext.tracker)
    }

    fun Fragment.trackScreen() {
        context?.trackScreen(this::class.java.name, this::class.java.simpleName)
    }
    //endregion

    //region Track events
    fun Fragment.trackEvent(category: String, name: String, action: TrackerAction = TrackerAction.CLICK, value: Float? = null) {
        context?.trackEvent(category, name, action, value)
    }

    fun Context.trackEvent(category: String, name: String, action: TrackerAction = TrackerAction.CLICK, value: Float? = null) {
        TrackHelper.track()
            .event(category, action.toString())
            .name(name)
            .let { if (value != null) it.value(value) else it }
            .with(applicationContext.tracker)
    }

    fun Context.trackAccountEvent(name: String, action: TrackerAction = TrackerAction.CLICK, value: Float? = null) {
        trackEvent("account", name, action, value)
    }
    //endregion

    fun Boolean.toFloat() = if (this) 1.0f else 0.0f

    enum class TrackerAction {
        CLICK,
        DATA,
        DRAG,
        INPUT,
        LONG_PRESS;

        override fun toString() = name.lowercase().snakeToCamelCase()
    }
}
