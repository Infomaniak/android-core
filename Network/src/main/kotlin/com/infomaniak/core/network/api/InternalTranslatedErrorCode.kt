/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.core.network.api

import androidx.annotation.StringRes
import com.infomaniak.core.network.R
import com.infomaniak.core.network.utils.ErrorCodeTranslated

enum class InternalTranslatedErrorCode(
    override val code: String,
    @StringRes override val translateRes: Int,
) : ErrorCodeTranslated {
    NoConnection("no_connection", R.string.noConnection),
    ConnectionError("connection_error", R.string.connectionError),
    UnknownError("an_error_has_occurred", R.string.anErrorHasOccurred),
    UserLoggedOut("user_logged_out", R.string.anErrorHasOccurred), // We don't display this error
    UserAlreadyPresent("user_already_present", R.string.errorUserAlreadyPresent),
}
