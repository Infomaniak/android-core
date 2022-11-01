/*
 * Infomaniak Core - Android
 * Copyright (C) 2022 Infomaniak Network SA
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
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.request.Disposable
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.button.MaterialButton
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.utils.UtilsUi.generateInitialsAvatarDrawable
import com.infomaniak.lib.core.utils.UtilsUi.getBackgroundColorBasedOnId
import org.apache.commons.cli.MissingArgumentException
import java.text.SimpleDateFormat
import java.util.*

const val FORMAT_DATE_CLEAR_MONTH = "dd MMM yyyy"
const val FORMAT_DATE_CLEAR_MONTH_DAY_ONE_CHAR = "d MMM yyyy"
const val FORMAT_DATE_DEFAULT = "dd.MM.yy"
const val FORMAT_DATE_HOUR_MINUTE = "HH:mm"
const val FORMAT_DATE_SHORT_DAY_ONE_CHAR = "d MMM"
const val FORMAT_EVENT_DATE = "dd/MM/yyyy HH:mm"
const val FORMAT_FULL_DATE = "EEEE dd MMMM yyyy"
const val FORMAT_NEW_FILE = "yyyyMMdd_HHmmss"

fun Intent.clearStack() = apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }

fun Date.format(pattern: String = FORMAT_DATE_DEFAULT): String {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}

fun Date.startOfTheDay(): Date =
    Calendar.getInstance().apply {
        time = this@startOfTheDay
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.time

fun Date.endOfTheDay(): Date =
    Calendar.getInstance().apply {
        time = this@endOfTheDay
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.time

fun Date.startOfTomorrow(): Date =
    Calendar.getInstance().apply {
        time = this@startOfTomorrow
        add(Calendar.DATE, 1)
    }.time.startOfTheDay()

fun Date.endOfTomorrow(): Date =
    Calendar.getInstance().apply {
        time = this@endOfTomorrow
        add(Calendar.DATE, 1)
    }.time.endOfTheDay()

fun Date.startOfTheWeek(): Date =
    Calendar.getInstance().apply {
        time = this@startOfTheWeek
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }.time.startOfTheDay()

fun Date.endOfTheWeek(): Date =
    Calendar.getInstance().apply {
        time = this@endOfTheWeek
        set(Calendar.DAY_OF_WEEK, (firstDayOfWeek - 1 + 6) % 7 + 1)
    }.time.endOfTheDay()

fun Date.year(): Int =
    Calendar.getInstance().apply {
        time = this@year
    }.get(Calendar.YEAR)

fun Date.month(): Int =
    Calendar.getInstance().apply {
        time = this@month
    }.get(Calendar.MONTH)

fun Date.day(): Int =
    Calendar.getInstance().apply {
        time = this@day
    }.get(Calendar.DAY_OF_MONTH)

fun Date.hours(): Int =
    Calendar.getInstance().apply {
        time = this@hours
    }.get(Calendar.HOUR_OF_DAY)

fun Date.minutes(): Int =
    Calendar.getInstance().apply {
        time = this@minutes
    }.get(Calendar.MINUTE)

fun Date.isToday(): Boolean = Date().let { now -> year() == now.year() && month() == now.month() && day() == now.day() }

fun Date.isYesterday(): Boolean {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time
    return year() == yesterday.year() && month() == yesterday.month() && day() == yesterday.day()
}

fun Date.isThisWeek(): Boolean {
    val now = Date()
    return this in now.startOfTheWeek()..now.endOfTheWeek()
}

fun Date.isThisMonth(): Boolean = Date().let { now -> year() == now.year() && month() == now.month() }

fun Date.isThisYear(): Boolean = Date().let { now -> year() == now.year() }

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Context.showToast(title: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, title, duration).show()
}

fun Context.showToast(title: Int, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, title, duration).show()
}

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
    findFirstVisibleItemPosition: (() -> Int)? = null,
    triggerOffset: Int = 3
): RecyclerView.OnScrollListener {
    if (layoutManager == null) throw Exception("This RecyclerView doesn't contains a LinearLayoutManager")

    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = with(recyclerView.layoutManager!!) {
            if (dy >= 0) {
                val visibleItemCount = childCount
                val totalItemCount = itemCount
                val pastVisibleItems = findFirstVisibleItemPosition?.invoke()
                    ?: (this as? LinearLayoutManager)?.findFirstVisibleItemPosition()?.plus(triggerOffset)
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
}

fun Context.goToPlaystore() {
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
    Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        addCategory(Intent.CATEGORY_DEFAULT)
        data = Uri.parse("package:$packageName")
        startActivity(this)
    }
}

fun Exception.isNetworkException() = this.javaClass.name.contains("java.net.", ignoreCase = true) ||
        this.javaClass.name.contains("javax.net.", ignoreCase = true) ||
        this is java.io.InterruptedIOException ||
        this is okhttp3.internal.http2.StreamResetException ||
        (this is java.io.IOException && this.message == "stream closed") // Okhttp3

fun String.firstOrEmpty(): Char = if (isNotEmpty()) first() else Char.MIN_VALUE

fun Window.toggleEdgeToEdge(enabled: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(this, !enabled)
}

fun Window.lightStatusBar(enabled: Boolean) {
    // TODO: DOESN'T WORK
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    //     if (enabled) {
    //         insetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS)
    //     } else {
    //         insetsController?.setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS)
    //     }
    // } else
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (enabled) {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}

fun Window.lightNavigationBar(enabled: Boolean) {
    //TODO Android 11
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (enabled) {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }
}

fun View.hideKeyboard() {
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.showKeyboard() {
    if (requestFocus()) {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            showSoftInput(this@showKeyboard, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

fun String.capitalizeFirstChar(): String = replaceFirstChar { char -> char.titlecase() }

fun SharedPreferences.transaction(block: SharedPreferences.Editor.() -> Unit) {
    with(edit()) {
        block(this)
        apply()
    }
}

fun ImageView.loadAvatar(
    user: User,
    imageLoader: ImageLoader = ImageLoader.Builder(context).build()
): Disposable = loadAvatar(user.id, user.avatar, user.getInitials(), imageLoader)

fun ImageView.loadAvatar(
    id: Int,
    avatarUrl: String?,
    initials: String,
    imageLoader: ImageLoader = ImageLoader.Builder(context).build()
): Disposable {
    val fallback = context.generateInitialsAvatarDrawable(
        initials = initials,
        background = context.getBackgroundColorBasedOnId(id),
    )
    return load(avatarUrl, imageLoader) {
        error(fallback)
        fallback(fallback)
        placeholder(R.drawable.placeholder)
    }
}

fun Fragment.canNavigate(currentClassName: String? = null): Boolean {
    val className = currentClassName ?: when (val currentDestination = findNavController().currentDestination) {
        is FragmentNavigator.Destination -> currentDestination.className
        is DialogFragmentNavigator.Destination -> currentDestination.className
        else -> null
    }

    return javaClass.name == className
}

fun Fragment.safeNavigate(directions: NavDirections, currentClassName: String? = null) {
    if (canNavigate(currentClassName)) findNavController().navigate(directions)
}

fun Fragment.safeNavigate(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    currentClassName: String? = null
) {
    if (canNavigate(currentClassName)) findNavController().navigate(resId, args, navOptions, navigatorExtras)
}

fun <T> List<T>.isContentEqual(other: List<T>, predicate: (T, T) -> Boolean): Boolean {
    if (size != other.size) return false

    forEachIndexed { index, item -> if (!predicate(item, other[index])) return false }

    return true
}

operator fun Regex.contains(input: String) = containsMatchIn(input)

fun AttributeSet.getAttributes(
    context: Context,
    @StyleableRes styleableResource: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
    block: TypedArray.() -> Unit
) {
    context.obtainStyledAttributes(this, styleableResource, defStyleAttr, defStyleRes).apply {
        block()
        recycle()
    }
}
