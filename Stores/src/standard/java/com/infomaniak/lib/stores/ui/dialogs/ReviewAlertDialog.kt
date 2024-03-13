/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
package com.infomaniak.lib.stores.ui.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ReviewAlertDialog(
    private val activityContext: Context,
    customThemeRes: Int?,
    reviewAlertDialogData: ReviewAlertDialogData,
) {

    private val alertDialog: AlertDialog

    init {
        val builder: MaterialAlertDialogBuilder = customThemeRes?.let { theme ->
            MaterialAlertDialogBuilder(activityContext, theme)
        } ?: MaterialAlertDialogBuilder(activityContext)

        with(reviewAlertDialogData) {

            alertDialog = builder
                .setTitle(reviewAlertDialogData.title)
                .setPositiveButton(positiveText) { _, _ -> onPositiveButtonClicked() }
                .setNegativeButton(negativeText) { _, _ -> onNegativeButtonClicked() }
                .setCancelable(false)
                .create()
        }
    }

    fun show() {
        alertDialog.show()
    }
}

data class ReviewAlertDialogData(
    val title: String,
    val positiveText: String,
    val negativeText: String,
    val onPositiveButtonClicked: () -> Unit,
    val onNegativeButtonClicked: () -> Unit,
)
