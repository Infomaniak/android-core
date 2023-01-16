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

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class FilePicker {
    private var _callback: (uris: List<Uri>) -> Unit = {}

    private val activityResult: ActivityResultLauncher<String>

    constructor(fragment: Fragment) {
        activityResult = fragment.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            _callback(uris)
        }
    }

    constructor(activity: ComponentActivity) {
        activityResult = activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            _callback(uris)
        }
    }

    fun open(mimeType: String = "*/*", callback: (List<Uri>) -> Unit) {
        _callback = callback
        activityResult.launch(mimeType)
    }
}
