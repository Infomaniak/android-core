/*
 * Infomaniak Core - Android
 * Copyright (C) 2026 Infomaniak Network SA
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
package com.infomaniak.core.ui.view

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.infomaniak.core.R as RCore

object SnackbarUtils {

    @JvmStatic
    fun Fragment.showSnackbar(
        @StringRes title: Int,
        anchor: View? = null,
        @StringRes actionButtonTitle: Int = RCore.string.buttonCancel,
        onActionClicked: (() -> Unit)? = null,
    ) {
        activity?.showSnackbar(
            title = title,
            anchor = anchor,
            actionButtonTitle = actionButtonTitle,
            onActionClicked = onActionClicked,
        )
    }

    @JvmStatic
    fun Fragment.showSnackbar(
        title: String,
        anchor: View? = null,
        @StringRes actionButtonTitle: Int = RCore.string.buttonCancel,
        onActionClicked: (() -> Unit)? = null,
    ) {
        activity?.showSnackbar(
            title = title,
            anchor = anchor,
            actionButtonTitle = actionButtonTitle,
            onActionClicked = onActionClicked,
        )
    }

    @JvmStatic
    fun Activity.showSnackbar(
        @StringRes title: Int,
        anchor: View? = null,
        @StringRes actionButtonTitle: Int = RCore.string.buttonCancel,
        onActionClicked: (() -> Unit)? = null,
    ) {
        showSnackbar(
            view = window.decorView.findViewById(android.R.id.content),
            title = title,
            anchor = anchor,
            actionButtonTitle = actionButtonTitle,
            onActionClicked = onActionClicked,
        )
    }

    @JvmStatic
    fun Activity.showSnackbar(
        title: String,
        anchor: View? = null,
        @StringRes actionButtonTitle: Int = RCore.string.buttonCancel,
        onActionClicked: (() -> Unit)? = null,
    ) {
        showSnackbar(
            view = window.decorView.findViewById(android.R.id.content),
            title = title,
            anchor = anchor,
            actionButtonTitle = actionButtonTitle,
            onActionClicked = onActionClicked,
        )
    }

    @JvmStatic
    fun Activity.showIndefiniteSnackbar(
        @StringRes title: Int,
        anchor: View? = null,
        @StringRes actionButtonTitle: Int = RCore.string.buttonCancel,
        onActionClicked: (() -> Unit)? = null,
    ) = showSnackbar(
        view = window.decorView.findViewById(android.R.id.content),
        anchor = anchor,
        title = title,
        actionButtonTitle = actionButtonTitle,
        length = Snackbar.LENGTH_INDEFINITE,
        onActionClicked = onActionClicked,
    )

    @JvmStatic
    fun showSnackbar(
        view: View,
        @StringRes title: Int,
        anchor: View? = null,
        length: Int = Snackbar.LENGTH_LONG,
        @StringRes actionButtonTitle: Int = RCore.string.buttonCancel,
        onActionClicked: (() -> Unit)? = null,
    ) = showSnackbar(
        view = view,
        title = view.context.getString(title),
        anchor = anchor,
        actionButtonTitle = actionButtonTitle,
        length = length,
        onActionClicked = onActionClicked,
    )

    @JvmStatic
    fun showSnackbar(
        view: View,
        title: String,
        anchor: View? = null,
        @StringRes actionButtonTitle: Int = RCore.string.buttonCancel,
        length: Int = Snackbar.LENGTH_LONG,
        onActionClicked: (() -> Unit)? = null,
    ) = Snackbar.make(view, title, length).apply {
        anchor?.let { anchorView = it }
        onActionClicked?.let { action -> setAction(actionButtonTitle) { action() } }
        show()
    }
}
