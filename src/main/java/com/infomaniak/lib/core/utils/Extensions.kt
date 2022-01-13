/*
 * Infomaniak Core - Android
 * Copyright (C) 2021 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.button.MaterialButton
import org.apache.commons.cli.MissingArgumentException
import java.text.SimpleDateFormat
import java.util.*

const val FORMAT_DATE_CLEAR_MONTH = "dd MMM yyyy"
const val FORMAT_DATE_DEFAULT = "dd.MM.yy"
const val FORMAT_DATE_HOUR_MINUTE = "HH:mm"
const val FORMAT_EVENT_DATE = "dd/MM/yyyy HH:mm"
const val FORMAT_FULL_DATE = "EEEE dd MMMM yyyy"
const val FORMAT_NEW_FILE = "yyyyMMdd_HHmmss"

fun Date.format(pattern: String = FORMAT_DATE_DEFAULT): String {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun MaterialButton.initProgress(lifecycle: LifecycleOwner) {
    apply {
        lifecycle.bindProgressButton(this)
        attachTextChangeAnimator()
    }
}

fun MaterialButton.showProgress(color: Int = Color.WHITE) {
    apply {
        isClickable = false
        showProgress {
            progressColor = color
        }
    }
}

fun MaterialButton.hideProgress(@StringRes text: Int) {
    apply {
        isClickable = true
        hideProgress(text)
    }
}

/**
 * Set a pagination in a RecyclerView, only if a LinearLayoutManager has been attached
 * @param whenLoadMoreIsPossible A callback which will be called each time it is necessary to load more items
 */
fun RecyclerView.setPagination(
    whenLoadMoreIsPossible: () -> Unit,
    findFirstVisibleItemPosition: (() -> Int)? = null
): RecyclerView.OnScrollListener {
    layoutManager?.let {
        val listener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val visibleItemCount = it.childCount
                    val totalItemCount = it.itemCount
                    val pastVisibleItems = findFirstVisibleItemPosition?.invoke()
                        ?: (it as? LinearLayoutManager)?.findFirstVisibleItemPosition()?.plus(3)
                        ?: throw MissingArgumentException("Missing findFirstVisibleItemPosition callback")
                    val isLastElement = (visibleItemCount + pastVisibleItems) >= totalItemCount

                    if (isLastElement) {
                        this@setPagination.post {
                            whenLoadMoreIsPossible()
                        }
                    }
                }
            }
        }
        addOnScrollListener(listener)
        return listener
    } ?: throw Exception("This RecyclerView doesn't contains a LinearLayoutManager")
}

fun Context.gotToPlaystore() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (anfe: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}

fun Context.hasPermissions(permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.requestPermissionsIsPossible(permissions: Array<String>): Boolean {
    return permissions.all {
        ActivityCompat.shouldShowRequestPermissionRationale(this, it)
    }
}

fun Context.startAppSettingsConfig() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.data = Uri.parse("package:$packageName")
    startActivity(intent)
}

fun Exception.isNetworkException() = this.javaClass.name.contains("java.net.", ignoreCase = true) ||
        this.javaClass.name.contains("javax.net.", ignoreCase = true) ||
        this is java.io.InterruptedIOException ||
        this is okhttp3.internal.http2.StreamResetException ||
        (this is java.io.IOException && this.message == "stream closed") // Okhttp3