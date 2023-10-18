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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.icu.text.Normalizer2
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager.*
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.ImageLoader
import coil.load
import coil.request.Disposable
import com.github.razir.progressbutton.*
import com.github.razir.progressbutton.DrawableButton.Companion.GRAVITY_CENTER
import com.github.razir.progressbutton.hideProgress
import com.google.android.material.button.MaterialButton
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.utils.CoilUtils.simpleImageLoader
import com.infomaniak.lib.core.utils.Utils.ACCENTS_PATTERN
import com.infomaniak.lib.core.utils.Utils.CAMEL_CASE_REGEX
import com.infomaniak.lib.core.utils.Utils.SNAKE_CASE_REGEX
import com.infomaniak.lib.core.utils.UtilsUi.generateInitialsAvatarDrawable
import com.infomaniak.lib.core.utils.UtilsUi.getBackgroundColorBasedOnId
import org.apache.commons.cli.MissingArgumentException
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun Intent.clearStack() = apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }

@SuppressLint("QueryPermissionsNeeded")
fun Intent.hasSupportedApplications(context: Context) = resolveActivity(context.packageManager) != null

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Context.showToast(title: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, title, duration).show()
}

fun Context.showToast(title: Int, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, title, duration).show()
}

fun MaterialButton.initProgress(lifecycle: LifecycleOwner) {
    lifecycle.bindProgressButton(button = this)
    attachTextChangeAnimator()
}

fun MaterialButton.showProgress(color: Int = Color.WHITE) {
    isClickable = false
    showProgress {
        progressColor = color
        gravity = GRAVITY_CENTER
    }
}

fun MaterialButton.hideProgress(@StringRes text: Int) {
    isClickable = true
    hideProgress(text)
}

/**
 * Set a pagination in a RecyclerView, only if a LinearLayoutManager has been attached
 * @param whenLoadMoreIsPossible A callback which will be called each time it is necessary to load more items
 */
fun RecyclerView.setPagination(
    whenLoadMoreIsPossible: () -> Unit,
    findFirstVisibleItemPosition: (() -> Int)? = null,
    triggerOffset: Int = 3,
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

fun Context.goToPlayStore(appPackageName: String = packageName) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
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
    }.also(::startActivity)
}

@SuppressLint("InlinedApi")
fun Context.openAppNotificationSettings() {
    Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            else -> {
                putExtra("app_package", packageName)
                putExtra("app_uid", applicationInfo.uid)
            }
        }
    }.also(::startActivity)
}

fun Context.getAppName() = packageManager.getApplicationLabel(applicationInfo).toString()

fun Exception.isNetworkException(): Boolean {
    val okHttpException = arrayOf("stream closed", "required settings preface not received")
    return this.javaClass.name.contains("java.net.", ignoreCase = true) ||
            this.javaClass.name.contains("javax.net.", ignoreCase = true) ||
            this is java.io.InterruptedIOException ||
            this is okhttp3.internal.http2.StreamResetException ||
            (this is java.io.IOException && this.message?.lowercase() in okHttpException)
}

fun String.firstOrEmpty(): String = if (isNotEmpty()) first().toString() else ""

fun Window.toggleEdgeToEdge(enabled: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(this, !enabled)
}

fun Context.isNightModeEnabled(): Boolean {
    return resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
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
    // TODO Android 11
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
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
            this@showKeyboard,
            InputMethodManager.SHOW_IMPLICIT,
        )
    }
}

fun Dialog.showKeyboard() {
    window?.apply {
        if (decorView.requestFocus()) setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }
}

fun View.setMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        setMargins(
            left ?: leftMargin,
            top ?: topMargin,
            right ?: rightMargin,
            bottom ?: bottomMargin,
        )
        requestLayout()
    }
}

fun View.setMarginsRelative(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        start?.let { marginStart = it }
        top?.let { topMargin = it }
        end?.let { marginEnd = it }
        bottom?.let { bottomMargin = it }
        requestLayout()
    }
}

fun View.setPaddingRelative(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        setPaddingRelative(
            start ?: paddingStart,
            top ?: paddingTop,
            end ?: paddingEnd,
            bottom ?: paddingBottom,
        )
        requestLayout()
    }
}

fun String.capitalizeFirstChar(): String = replaceFirstChar { char -> char.titlecase() }

fun String.guessMimeType(): String {
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(substringAfterLast(".")) ?: "*/*"
}

fun SharedPreferences.transaction(block: SharedPreferences.Editor.() -> Unit) {
    with(edit()) {
        block(this)
        apply()
    }
}

fun ImageView.loadAvatar(
    user: User,
    imageLoader: ImageLoader = context.simpleImageLoader,
): Disposable = loadAvatar(user.id, user.avatar, user.getInitials(), imageLoader)

fun ImageView.loadAvatar(
    id: Int,
    avatarUrl: String?,
    initials: String,
    imageLoader: ImageLoader = context.simpleImageLoader,
    @ColorInt initialsColor: Int = Color.WHITE,
): Disposable {
    val backgroundColor = context.getBackgroundColorBasedOnId(id)
    return loadAvatar(backgroundColor, avatarUrl, initials, imageLoader, initialsColor)
}

fun ImageView.loadAvatar(
    backgroundColor: GradientDrawable,
    avatarUrl: String?,
    initials: String,
    imageLoader: ImageLoader = context.simpleImageLoader,
    @ColorInt initialsColor: Int = Color.WHITE,
): Disposable {
    val fallback = context.generateInitialsAvatarDrawable(
        initials = initials,
        background = backgroundColor,
        initialsColor = initialsColor,
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
        null -> javaClass.name
        else -> null
    }

    return javaClass.name == className
}

fun Fragment.safeNavigate(directions: NavDirections, currentClassName: String? = null) = with(findNavController()) {
    if (canNavigate(currentClassName) && currentDestination?.getAction(directions.actionId) != null) {
        navigate(directions)
    }
}

fun Fragment.safeNavigate(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    currentClassName: String? = null,
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
    block: TypedArray.() -> Unit,
) {
    context.obtainStyledAttributes(this, styleableResource, defStyleAttr, defStyleRes).apply {
        block()
        recycle()
    }
}

inline fun <reified T : Parcelable> Intent.parcelableExtra(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Intent.parcelableArrayListExtra(key: String): List<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

inline fun <reified T : Serializable> Intent.serializableExtra(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

fun ActivityResult.whenResultIsOk(completion: (Intent?) -> Unit) {
    if (resultCode == Activity.RESULT_OK) data.let(completion::invoke)
}

/**
 * Send a value to the previous navigation
 */
fun <T> Fragment.setBackNavigationResult(key: String, value: T) {
    findNavController().apply {
        previousBackStackEntry?.savedStateHandle?.set(key, value)
        popBackStack()
    }
}

/**
 * Observes and handles back navigation results using the given [key].
 *
 * This function adds an observer to the lifecycle events of the current back stack entry
 * and handles the specified [key] in the saved state handle when appropriate lifecycle
 * events occur. The [onResult] lambda is called with the result value associated with the key.
 *
 * @param key The key used to identify the result value in the saved state handle.
 * @param onResult Lambda function to be executed with the result value when available.
 *
 * @param T The type of the result value associated with the key.
 */
fun <T> Fragment.getBackNavigationResult(key: String, onResult: (result: T) -> Unit) {
    val backStackEntry = findNavController().currentBackStackEntry
    val observer = LifecycleEventObserver { _, event ->
        val lifecycleEventsToHandle = arrayOf(Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME)
        if (event in lifecycleEventsToHandle && backStackEntry?.savedStateHandle?.contains(key) == true) {
            backStackEntry.savedStateHandle.get<T>(key)?.let(onResult)
            backStackEntry.savedStateHandle.remove<T>(key)
        }
    }

    // Add observer to the back stack entry's lifecycle
    backStackEntry?.lifecycle?.addObserver(observer)

    // Remove observer when the view's lifecycle is being destroyed
    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) backStackEntry?.lifecycle?.removeObserver(observer)
    })
}

fun String.camelToSnakeCase() = replace(CAMEL_CASE_REGEX) { "_${it.value}" }.lowercase()

fun String.snakeToCamelCase() = replace(SNAKE_CASE_REGEX) { it.value.replace("_", "").uppercase() }

@RequiresApi(Build.VERSION_CODES.N)
fun String.removeAccents(): String {
    return ACCENTS_PATTERN.matcher(Normalizer2.getNFDInstance().normalize(this)).replaceAll("")
}

inline val ViewBinding.context: Context get() = root.context

fun <T> Fragment.safeBinding(): ReadWriteProperty<Fragment, T> {
    return object : ReadWriteProperty<Fragment, T>, DefaultLifecycleObserver {

        private var binding: T? = null
        private var viewLifecycleOwner: LifecycleOwner? = null

        init {
            viewLifecycleOwnerLiveData.observe(this@safeBinding) { lifecycleOwner ->
                viewLifecycleOwner?.lifecycle?.removeObserver(this)
                viewLifecycleOwner = lifecycleOwner
                lifecycleOwner.lifecycle.addObserver(this)
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            binding = null
        }

        override fun getValue(thisRef: Fragment, property: KProperty<*>): T = binding!!

        override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
            binding = value
        }
    }
}
