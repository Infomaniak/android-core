/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2025 Infomaniak Network SA
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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
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
import com.github.razir.progressbutton.DrawableButton.Companion.GRAVITY_CENTER
import com.github.razir.progressbutton.TextChangeAnimatorParams
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.detachTextChangeAnimator
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.button.MaterialButton
import com.google.gson.JsonSyntaxException
import com.infomaniak.lib.core.models.user.User
import com.infomaniak.lib.core.utils.CoilUtils.simpleImageLoader
import com.infomaniak.lib.core.utils.Utils.ACCENTS_PATTERN
import com.infomaniak.lib.core.utils.Utils.CAMEL_CASE_REGEX
import com.infomaniak.lib.core.utils.Utils.SNAKE_CASE_REGEX
import com.infomaniak.lib.core.utils.UtilsUi.generateInitialsAvatarDrawable
import com.infomaniak.lib.core.utils.UtilsUi.getBackgroundColorBasedOnId
import kotlinx.serialization.SerializationException
import org.apache.commons.cli.MissingArgumentException
import java.io.Serializable
import java.text.Normalizer
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

fun MaterialButton.initProgress(lifecycle: LifecycleOwner? = null, color: Int? = null) {

    lifecycle?.bindProgressButton(button = this)

    val params = color?.let {
        TextChangeAnimatorParams().apply {
            useCurrentTextColor = false
            textColor = color
            fadeInMills = 0L
            fadeOutMills = 0L
        }
    }

    params?.let(::attachTextChangeAnimator) ?: attachTextChangeAnimator()
}

fun MaterialButton.updateTextColor(color: Int?) {
    detachTextChangeAnimator()
    initProgress(color = color)
}

fun MaterialButton.showProgressCatching(color: Int? = null) {
    isClickable = false
    // showProgress stores references to views which crashes when the view is freed
    runCatching {
        showProgress {
            progressColor = color ?: Color.WHITE
            gravity = GRAVITY_CENTER
        }
    }
}

fun MaterialButton.hideProgressCatching(@StringRes text: Int) {
    isClickable = true
    // hideProgress stores references to views which crashes when the view is freed
    runCatching { hideProgress(text) }
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
        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
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

fun Exception.isSerializationException(): Boolean {
    return this is JsonSyntaxException || this is SerializationException || this is IllegalArgumentException
}

fun String.firstOrEmpty(): String = if (isNotEmpty()) first().toString() else ""

fun Window.toggleEdgeToEdge(enabled: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(this, !enabled)
}

fun Context.isNightModeEnabled(): Boolean {
    return resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

// TODO: Fix deprecated
fun Window.lightStatusBar(enabled: Boolean) {
    // TODO: DOESN'T WORK
    // if (SDK_INT >= 30) {
    //     if (enabled) {
    //         insetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS)
    //     } else {
    //         insetsController?.setSystemBarsAppearance(0, APPEARANCE_LIGHT_STATUS_BARS)
    //     }
    // } else {
    if (enabled) {
        decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }
    // }
}

// TODO: Fix deprecated
fun Window.lightNavigationBar(enabled: Boolean) {
    // TODO Android 11
    if (enabled) {
        decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    } else {
        decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
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

@Deprecated("Use the method exposed through the Core:Coil module")
fun ImageView.loadAvatar(
    user: User,
    imageLoader: ImageLoader = context.simpleImageLoader,
) = loadAvatar(user.id, user.avatar, user.getInitials(), imageLoader)

fun ImageView.loadAvatar(
    id: Int,
    avatarUrl: String?,
    initials: String,
    imageLoader: ImageLoader = context.simpleImageLoader,
    @ColorInt initialsColor: Int = Color.WHITE,
) {
    val backgroundColor = context.getBackgroundColorBasedOnId(id)
    loadAvatar(backgroundColor, avatarUrl, initials, imageLoader, initialsColor)
}

@Deprecated("Use the method exposed through the Core:Coil module")
fun ImageView.loadAvatar(
    backgroundColor: GradientDrawable,
    avatarUrl: String?,
    initials: String,
    imageLoader: ImageLoader = context.simpleImageLoader,
    @ColorInt initialsColor: Int = Color.WHITE,
) {
    val fallback = context.generateInitialsAvatarDrawable(
        initials = initials,
        background = backgroundColor,
        initialsColor = initialsColor,
    )
    load(avatarUrl, imageLoader) {
        error(fallback)
        fallback(fallback)
        placeholder(fallback)
    }
}

@Deprecated(
    "Providing a currentClassName will bypass any form of verification. Use the new method exposed through the FragmentNavigation module instead",
    ReplaceWith(
        expression = "isAtInitialDestination()",
        imports = ["com.infomaniak.core.fragmentnavigation.isAtInitialDestination"],
    )
)
fun Fragment.canNavigate(currentClassName: String? = null): Boolean {
    @Suppress("DEPRECATION")
    return findNavController().canNavigate(allowedStartingClassName = javaClass.name, currentClassName)
}

@Deprecated(
    "Providing a currentClassName will bypass any form of verification. Use the new method exposed through the FragmentNavigation module instead",
    ReplaceWith(
        expression = "isAtInitialDestination(allowedStartingClassName)",
        imports = ["com.infomaniak.core.fragmentnavigation.isAtInitialDestination"],
    )
)
fun NavController.canNavigate(allowedStartingClassName: String, currentClassName: String? = null): Boolean {
    val className = currentClassName ?: when (val currentDestination = currentDestination) {
        is FragmentNavigator.Destination -> currentDestination.className
        is DialogFragmentNavigator.Destination -> currentDestination.className
        null -> allowedStartingClassName
        else -> null
    }

    return allowedStartingClassName == className
}

@Deprecated(
    "Providing a currentClassName won't have any impact. Use the new method exposed through the FragmentNavigation module instead",
    ReplaceWith(
        expression = "safelyNavigate(directions)",
        imports = ["com.infomaniak.core.fragmentnavigation.safelyNavigate"],
    )
)
fun Fragment.safeNavigate(directions: NavDirections, currentClassName: String? = null) = with(findNavController()) {
    @Suppress("DEPRECATION")
    if (canNavigate(currentClassName) && currentDestination?.getAction(directions.actionId) != null) {
        navigate(directions)
    }
}

@Deprecated(
    "Providing a currentClassName will bypass any form of verification. Use the new method exposed through the FragmentNavigation module instead",
    ReplaceWith(
        expression = "safelyNavigate(resId, args, navOptions, navigatorExtras, currentClassName)",
        imports = ["com.infomaniak.core.fragmentnavigation.safelyNavigate"],
    )
)
fun Fragment.safeNavigate(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    currentClassName: String? = null,
) {
    @Suppress("DEPRECATION")
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
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Intent.parcelableArrayListExtra(key: String): List<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

inline fun <reified T : Serializable> Intent.serializableExtra(key: String): T? = when {
    SDK_INT >= 33 -> getSerializableExtra(key, T::class.java)
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
        val lifecycleEventsToHandle = arrayOf(Event.ON_START, Event.ON_RESUME)
        if (event in lifecycleEventsToHandle && backStackEntry?.savedStateHandle?.contains(key) == true) {
            backStackEntry.savedStateHandle.get<T>(key)?.let(onResult)
            backStackEntry.savedStateHandle.remove<T>(key)
        }
    }

    // Add observer to the back stack entry's lifecycle
    backStackEntry?.lifecycle?.addObserver(observer)

    // Remove observer when the view's lifecycle is being destroyed
    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Event.ON_DESTROY) backStackEntry?.lifecycle?.removeObserver(observer)
    })
}

fun String.camelToSnakeCase() = replace(CAMEL_CASE_REGEX) { "_${it.value}" }.lowercase()

fun String.snakeToCamelCase() = replace(SNAKE_CASE_REGEX) { it.value.replace("_", "").uppercase() }

fun String.removeAccents(): String = ACCENTS_PATTERN.matcher(Normalizer.normalize(this, Normalizer.Form.NFD)).replaceAll("")

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
